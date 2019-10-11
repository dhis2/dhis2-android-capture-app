package org.dhis2.usescases.login

import android.os.Build
import co.infinum.goldfinger.Goldfinger
import co.infinum.goldfinger.rx.RxGoldfinger
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.Constants.*
import org.dhis2.utils.analytics.*
import org.dhis2.utils.analytics.ACCOUNT_RECOVERY
import org.hisp.dhis.android.core.d2manager.D2Manager
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import retrofit2.Response
import timber.log.Timber

class LoginPresenter(private val view: LoginContracts.View,
                     private val preferenceProvider: PreferenceProvider,
                     private val schedulers: SchedulerProvider,
                     private val goldfinger: RxGoldfinger,
                     private val analyticsHelper: AnalyticsHelper) {

    private var userManager: UserManager? = null
    var disposable: CompositeDisposable = CompositeDisposable()

    private var canHandleBiometrics: Boolean? = null

    fun init(userManager: UserManager?) {
        this.userManager = userManager
        this.userManager?.let {
            disposable.add(it.isUserLoggedIn
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe({ isUserLoggedIn ->
                        if (isUserLoggedIn && !preferenceProvider.getBoolean(SESIONLOCKED, false)) {
                            view.startActivity(MainActivity::class.java, null, true, true, null)
                        } else if (preferenceProvider.getBoolean(SESIONLOCKED, false)) {
                            view.showUnlockButton()
                        }

                    }, { exception-> Timber.e(exception) }))
        } ?: view.setUrl(view.context.getString(R.string.login_https))
    }

    fun checkServerInfoAndShowBiometricButton(){
        userManager?.let { userManager ->
            disposable.add(
                    Observable.just(if (userManager.d2.systemInfoModule().systemInfo.blockingGet() != null)
                        userManager.d2.systemInfoModule().systemInfo.blockingGet()
                    else
                        SystemInfo.builder().build())
                            .subscribeOn(schedulers.io())
                            .observeOn(schedulers.ui())
                            .subscribe(
                                    { systemInfo ->
                                        if (systemInfo.contextPath() != null) {
                                            view.setUrl(systemInfo.contextPath() ?: "")
                                            preferenceProvider.getString(USER, "")?.also {
                                                view.setUser(it)
                                            }
                                        } else
                                            view.setUrl(view.context.getString(R.string.login_https))
                                    },
                                    { Timber.e(it) }))
        } ?: view.setUrl(view.context.getString(R.string.login_https))

        showBiometricButtonIfVersionIsGreaterThanM(view)
    }

    private fun showBiometricButtonIfVersionIsGreaterThanM(view: LoginContracts.View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            disposable.add(
                    Observable.just(goldfinger.hasEnrolledFingerprint())
                            .filter { canHandleBiometrics ->
                                this.canHandleBiometrics = canHandleBiometrics
                                canHandleBiometrics && preferenceProvider.contains(SECURE_SERVER_URL)
                            }
                            .subscribeOn(schedulers.io())
                            .observeOn(schedulers.ui())
                            .subscribe(
                                    { view.showBiometricButton() },
                                    { Timber.e(it) }))
        }
    }

    fun onButtonClick() {
        view.hideKeyboard()
        analyticsHelper.setEvent(LOGIN, CLICK, LOGIN)
        if (!preferenceProvider.getBoolean(USER_ASKED_CRASHLYTICS, false)){
            view.showCrashlyticsDialog()
        } else {
            view.showLoginProgress(true)
        }
    }

     fun logIn(serverUrl: String, userName: String, pass: String) {
        disposable.add(
                D2Manager.setServerUrl(serverUrl)
                        .andThen(D2Manager.instantiateD2())
                        .map { (view.abstracContext.applicationContext as App).createServerComponent().userManager() }
                        .flatMapObservable { userManager ->
                            preferenceProvider.setValue(SERVER, "$serverUrl/api")
                            this.userManager = userManager
                            userManager.logIn(userName.trim { it <= ' ' }, pass).map<Response<Any>> { user ->
                                run {
                                    with(preferenceProvider) {
                                        setValue(USER, user.userCredentials()?.username())
                                        setValue(SESIONLOCKED, false)
                                        setValue(PIN, null)
                                    }
                                    Response.success<Any>(null)
                                }
                            }

                        }
                        .subscribeOn(schedulers.io())
                        .observeOn(schedulers.ui())
                        .subscribe({ this.handleResponse(it) }, { this.handleError(it) }))
    }

    fun onQRClick() {
        analyticsHelper.setEvent(SERVER_QR_SCANNER, CLICK, SERVER_QR_SCANNER)
        view.navigateToQRActivity()
    }

    fun unlockSession(pin: String) {
        if (preferenceProvider.getString(PIN, "") == pin) {
            preferenceProvider.setValue(SESIONLOCKED, false)
            view.startActivity(MainActivity::class.java, null, true, true, null)
        }
    }

    fun onDestroy() {
        disposable.clear()
    }

     fun logOut() {
        userManager?.let {
            disposable.add(it.d2.userModule().logOut()
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe({
                                val prefs = view.abstracContext.sharedPreferences
                                prefs.edit().putBoolean(SESIONLOCKED, false).apply()
                                view.handleLogout()
                            },
                            { view.handleLogout() }
                    )
            )
        }
    }

    private fun handleResponse(userResponse: Response<*>) {
        view.showLoginProgress(false)
        if (userResponse.isSuccessful) {
            preferenceProvider.setValue(Preference.INITIAL_SYNC_DONE.name, false)
            view.saveUsersData()
        }
    }

     private fun handleError(throwable: Throwable) {
        Timber.e(throwable)
        if (throwable is D2Error && throwable.errorCode() == D2ErrorCode.ALREADY_AUTHENTICATED) {
            preferenceProvider.apply {
                setValue(SESIONLOCKED, false)
                setValue(PIN, null)
            }
            view.alreadyAuthenticated()
        } else {
            view.renderError(throwable)
        }
        view.showLoginProgress(false)
    }

    fun stopReadingFingerprint() {
        goldfinger.cancel()
    }

    fun canHandleBiometrics(): Boolean? {
        return canHandleBiometrics
    }

    fun onFingerprintClick() {
        view.showFingerprintDialog()
        disposable.add(
                goldfinger.authenticate()
                        .map { result ->
                            if (preferenceProvider.contains(SECURE_SERVER_URL,
                                            SECURE_USER_NAME, SECURE_PASS)) {
                                Result.success(result)
                            } else {
                                Result.failure(Exception(EMPTY_CREDENTIALS))
                            }
                        }
                        .observeOn(schedulers.ui())
                        .subscribe({
                                    if (it.isFailure){
                                        view.showEmptyCredentialsMessage()
                                    }
                                    else if (it.isSuccess && it.getOrNull()?.type() == Goldfinger.Type.SUCCESS){
                                        view.showCredentialsData(Goldfinger.Type.SUCCESS,
                                                preferenceProvider.getString(SECURE_SERVER_URL)!!,
                                                preferenceProvider.getString(SECURE_USER_NAME)!!,
                                                preferenceProvider.getString(SECURE_PASS)!!)
                                    }
                                    else {
                                        view.showCredentialsData(Goldfinger.Type.ERROR,
                                                it.getOrNull()?.message()!!)
                                    }
                                },
                                {
                                    view.displayMessage(AUTH_ERROR)
                                    view.hideFingerprintDialog()
                                }))
    }

    fun onAccountRecovery() {
        analyticsHelper.setEvent(ACCOUNT_RECOVERY, CLICK, ACCOUNT_RECOVERY)
        view.openAccountRecovery()
    }

    fun onUrlInfoClick() {
        view.displayAlertDialog()
    }

    companion object {
        const val SESIONLOCKED = "SessionLocked"
        const val PIN = "pin"
        const val EMPTY_CREDENTIALS = "Empty credentials"
        const val AUTH_ERROR = "AUTH ERROR"
    }
}