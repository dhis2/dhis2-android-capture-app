package org.dhis2.usescases.login

import android.os.Build
import co.infinum.goldfinger.Goldfinger
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.fingerprint.FingerPrintController
import org.dhis2.data.fingerprint.Type
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.Preference.Companion.PIN
import org.dhis2.data.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.Constants.PREFS_URLS
import org.dhis2.utils.Constants.PREFS_USERS
import org.dhis2.utils.Constants.SECURE_PASS
import org.dhis2.utils.Constants.SECURE_SERVER_URL
import org.dhis2.utils.Constants.SECURE_USER_NAME
import org.dhis2.utils.Constants.SERVER
import org.dhis2.utils.Constants.USER
import org.dhis2.utils.Constants.USER_ASKED_CRASHLYTICS
import org.dhis2.utils.Constants.USER_TEST_ANDROID
import org.dhis2.utils.TestingCredential
import org.dhis2.utils.analytics.ACCOUNT_RECOVERY
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.LOGIN
import org.dhis2.utils.analytics.SERVER_QR_SCANNER
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import retrofit2.Response
import timber.log.Timber

class LoginPresenter(
    private val view: LoginContracts.View,
    private val preferenceProvider: PreferenceProvider,
    private val schedulers: SchedulerProvider,
    private val fingerPrintController: FingerPrintController,
    private val analyticsHelper: AnalyticsHelper
) {

    private var userManager: UserManager? = null
    var disposable: CompositeDisposable = CompositeDisposable()

    private var canHandleBiometrics: Boolean? = null

    fun init(userManager: UserManager?) {
        this.userManager = userManager
        this.userManager?.let {
            disposable.add(
                it.isUserLoggedIn
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        { isUserLoggedIn ->
                            if (isUserLoggedIn && !preferenceProvider.getBoolean(
                                SESSION_LOCKED,
                                false
                            )
                            ) {
                                view.startActivity(MainActivity::class.java, null, true, true, null)
                            } else if (preferenceProvider.getBoolean(SESSION_LOCKED, false)) {
                                view.showUnlockButton()
                            }
                        },
                        { exception -> Timber.e(exception) }
                    )
            )
        } ?: view.setUrl(view.context.getString(R.string.login_https))
    }

    fun checkServerInfoAndShowBiometricButton() {
        userManager?.let { userManager ->
            disposable.add(
                Observable.just(
                    if (userManager.d2.systemInfoModule().systemInfo().blockingGet() != null) {
                        userManager.d2.systemInfoModule().systemInfo().blockingGet()
                    } else {
                        SystemInfo.builder().build()
                    }
                )
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        { systemInfo ->
                            if (systemInfo.contextPath() != null) {
                                view.setUrl(systemInfo.contextPath() ?: "")
                                preferenceProvider.getString(USER, "")?.also {
                                    view.setUser(it)
                                }
                            } else {
                                view.setUrl(view.context.getString(R.string.login_https))
                            }
                        },
                        { Timber.e(it) }
                    )
            )
        } ?: view.setUrl(view.context.getString(R.string.login_https))

        showBiometricButtonIfVersionIsGreaterThanM(view)
    }

    private fun showBiometricButtonIfVersionIsGreaterThanM(view: LoginContracts.View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            disposable.add(
                Observable.just(fingerPrintController.hasFingerPrint())
                    .filter { canHandleBiometrics ->
                        this.canHandleBiometrics = canHandleBiometrics
                        canHandleBiometrics && preferenceProvider.contains(SECURE_SERVER_URL)
                    }
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        { view.showBiometricButton() },
                        { Timber.e(it) }
                    )
            )
        }
    }

    fun onButtonClick() {
        view.hideKeyboard()
        analyticsHelper.setEvent(LOGIN, CLICK, LOGIN)
        if (!preferenceProvider.getBoolean(USER_ASKED_CRASHLYTICS, false)) {
            view.showCrashlyticsDialog()
        } else {
            view.showLoginProgress(true)
        }
    }

    fun logIn(serverUrl: String, userName: String, pass: String) {
        disposable.add(
            Observable.just(
                (view.abstracContext.applicationContext as App).createServerComponent()
                    .userManager()
            )
                .flatMap { userManager ->
                    preferenceProvider.setValue(SERVER, "$serverUrl/api")
                    this.userManager = userManager
                    userManager.logIn(userName.trim { it <= ' ' }, pass, serverUrl)
                        .map<Response<Any>> { user ->
                            run {
                                with(preferenceProvider) {
                                    setValue(
                                        USER,
                                        userManager.d2.userModule()
                                            .userCredentials()
                                            .blockingGet()
                                            .username()
                                    )
                                    setValue(SESSION_LOCKED, false)
                                    setValue(PIN, null)
                                }
                                Response.success<Any>(null)
                            }
                        }
                }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    {
                        this.handleResponse(it, userName, serverUrl)
                    },
                    {
                        this.handleError(it)
                    }
                )
        )
    }

    fun onQRClick() {
        analyticsHelper.setEvent(SERVER_QR_SCANNER, CLICK, SERVER_QR_SCANNER)
        view.navigateToQRActivity()
    }

    fun unlockSession(pin: String) {
        if (preferenceProvider.getString(PIN, "") == pin) {
            preferenceProvider.setValue(SESSION_LOCKED, false)
            view.startActivity(MainActivity::class.java, null, true, true, null)
        }
    }

    fun onDestroy() {
        disposable.clear()
    }

    fun logOut() {
        userManager?.let {
            disposable.add(
                it.d2.userModule().logOut()
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        {
                            val prefs = view.abstracContext.sharedPreferences
                            prefs.edit().putBoolean(SESSION_LOCKED, false).apply()
                            view.handleLogout()
                        },
                        { view.handleLogout() }
                    )
            )
        }
    }

    private fun handleResponse(userResponse: Response<*>, userName: String, server: String) {
        view.showLoginProgress(false)
        if (userResponse.isSuccessful) {
            preferenceProvider.setValue(Preference.INITIAL_SYNC_DONE, false)

            val updatedServer =
                (preferenceProvider.getSet(PREFS_URLS, HashSet()) as HashSet).add(userName)
            val updatedUsers =
                (preferenceProvider.getSet(PREFS_USERS, HashSet()) as HashSet).add(server)

            preferenceProvider.setValue(PREFS_URLS, updatedServer)
            preferenceProvider.setValue(PREFS_USERS, updatedUsers)

            view.saveUsersData()
        }
    }

    private fun handleError(throwable: Throwable) {
        Timber.e(throwable)
        if (throwable is D2Error && throwable.errorCode() == D2ErrorCode.ALREADY_AUTHENTICATED) {
            preferenceProvider.apply {
                setValue(SESSION_LOCKED, false)
                setValue(PIN, null)
            }
            view.alreadyAuthenticated()
        } else {
            view.renderError(throwable)
        }
        view.showLoginProgress(false)
    }

    fun stopReadingFingerprint() {
        fingerPrintController.cancel()
    }

    fun canHandleBiometrics(): Boolean? {
        return canHandleBiometrics
    }

    fun onFingerprintClick() {
        view.showFingerprintDialog()
        disposable.add(

            fingerPrintController.authenticate()
                .map { result ->
                    if (preferenceProvider.contains(
                        SECURE_SERVER_URL,
                        SECURE_USER_NAME,
                        SECURE_PASS
                    )
                    ) {
                        Result.success(result)
                    } else {
                        Result.failure(Exception(EMPTY_CREDENTIALS))
                    }
                }
                .observeOn(schedulers.ui())
                .subscribe(
                    {
                        if (it.isFailure) {
                            view.showEmptyCredentialsMessage()
                        } else if (it.isSuccess && it.getOrNull()?.type == Type.SUCCESS) {
                            view.showCredentialsData(
                                Goldfinger.Type.SUCCESS,
                                preferenceProvider.getString(SECURE_SERVER_URL)!!,
                                preferenceProvider.getString(SECURE_USER_NAME)!!,
                                preferenceProvider.getString(SECURE_PASS)!!
                            )
                        } else if (it.getOrNull()?.type == Type.ERROR) {
                            view.showCredentialsData(
                                Goldfinger.Type.ERROR,
                                it.getOrNull()?.message!!
                            )
                        }
                    },
                    {
                        view.displayMessage(AUTH_ERROR)
                        view.hideFingerprintDialog()
                    }
                )
        )
    }

    fun onAccountRecovery() {
        analyticsHelper.setEvent(ACCOUNT_RECOVERY, CLICK, ACCOUNT_RECOVERY)
        view.openAccountRecovery()
    }

    fun onUrlInfoClick() {
        view.displayAlertDialog()
    }

    fun getAutocompleteData(
        testingCredentials: List<TestingCredential>
    ): Pair<MutableList<String>, MutableList<String>> {
        val urls = preferenceProvider.getSet(PREFS_URLS, emptySet())!!.toMutableList()
        val users = preferenceProvider.getSet(PREFS_USERS, emptySet())!!.toMutableList()

        urls.let {
            for (testingCredential in testingCredentials) {
                if (!it.contains(testingCredential.server_url)) {
                    it.add(testingCredential.server_url)
                }
            }
        }

        preferenceProvider.setValue(PREFS_URLS, HashSet(urls))

        users.let {
            if (!it.contains(USER_TEST_ANDROID)) {
                it.add(USER_TEST_ANDROID)
            }
        }

        preferenceProvider.setValue(PREFS_USERS, HashSet(users))

        return Pair(urls, users)
    }

    companion object {
        const val EMPTY_CREDENTIALS = "Empty credentials"
        const val AUTH_ERROR = "AUTH ERROR"
    }
}
