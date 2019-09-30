package org.dhis2.usescases.login

import android.content.Intent
import android.os.Build
import android.view.View
import co.infinum.goldfinger.rx.RxGoldfinger
import de.adorsys.android.securestoragelibrary.SecurePreferences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.dhis2.App
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.data.prefs.Preference
import org.dhis2.data.server.UserManager
import org.dhis2.data.sharedPreferences.SharePreferencesProvider
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.qrScanner.QRActivity
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.d2manager.D2Manager
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import retrofit2.Response
import timber.log.Timber

class LoginPresenter internal constructor(val sharePreferencesProvider: SharePreferencesProvider) : LoginContracts.Presenter {

    override fun stopReadingFingerprint() {
        goldfinger.cancel()
    }

    private lateinit var view: LoginContracts.View
    private var userManager: UserManager? = null
    private lateinit var disposable: CompositeDisposable

    private var canHandleBiometrics: Boolean? = null
    private lateinit var goldfinger: RxGoldfinger

    override fun init(view: LoginContracts.View) {
        this.view = view
        view.setPreference(sharePreferencesProvider)
        this.disposable = CompositeDisposable()
        goldfinger = RxGoldfinger.Builder(view.context).setLogEnabled(BuildConfig.DEBUG).build()
        if ((view.context.applicationContext as App).serverComponent != null)
            userManager = (view.context.applicationContext as App).serverComponent.userManager()

        userManager?.let { userManager ->
            disposable.add(userManager.isUserLoggedIn
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ isUserLoggedIn ->
                        if (isUserLoggedIn && !sharePreferencesProvider.sharedPreferences().getBoolean("SessionLocked", false)!!) {
                            view.startActivity(MainActivity::class.java, null, true, true, null)
                        } else if (sharePreferencesProvider.sharedPreferences().getBoolean("SessionLocked", false)!!) {
                            view.showUnlockButton()
                        }

                    }, { Timber.e(it) }))

            disposable.add(
                    Observable.just(if (userManager.d2.systemInfoModule().systemInfo.blockingGet() != null)
                        userManager.d2.systemInfoModule().systemInfo.blockingGet()
                    else
                        SystemInfo.builder().build())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { systemInfo ->
                                        if (systemInfo.contextPath() != null) {
                                            view.setUrl(systemInfo.contextPath() ?: "")
                                            view.setUser(sharePreferencesProvider.sharedPreferences().getString(Constants.USER, "")!!)
                                        } else
                                            view.setUrl(view.context.getString(R.string.login_https))
                                    },
                                    { Timber.e(it) }))
        } ?: view.setUrl(view.context.getString(R.string.login_https))


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)

            disposable.add(
                    Observable.just(goldfinger.hasEnrolledFingerprint())
                            .filter { canHandleBiometrics ->
                                this.canHandleBiometrics = canHandleBiometrics
                                canHandleBiometrics && SecurePreferences.contains(Constants.SECURE_SERVER_URL)
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { view.showBiometricButton() },
                                    { Timber.e(it) }))

    }

    override fun onButtonClick() {
        view.hideKeyboard()
        if (!sharePreferencesProvider.sharedPreferences().getBoolean(Constants.USER_ASKED_CRASHLYTICS, false)!!)
            view.showCrashlyticsDialog()
        else
            view.showLoginProgress(true)
    }

    override fun logIn(serverUrl: String, userName: String, pass: String) {
        disposable.add(
                D2Manager.setServerUrl(serverUrl)
                        .andThen(D2Manager.instantiateD2())
                        .map { (view.abstracContext.applicationContext as App).createServerComponent().userManager() }
                        .flatMapObservable { userManager ->
                            sharePreferencesProvider.sharedPreferences().putString(Constants.SERVER, serverUrl + "/api")
                            this.userManager = userManager
                            userManager.logIn(userName.trim { it <= ' ' }, pass).map<Response<Any>> { user ->
                                run {
                                    sharePreferencesProvider.sharedPreferences().putString(Constants.USER, user.userCredentials()?.username())
                                    sharePreferencesProvider.sharedPreferences().putBoolean("SessionLocked", false)
                                    sharePreferencesProvider.sharedPreferences().putString("pin", null)
                                    Response.success<Any>(null)
                                }
                            }

                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { this.handleResponse(it) },
                                { this.handleError(it) }))
    }

    override fun onQRClick(v: View) {
        val intent = Intent(view.context, QRActivity::class.java)
        view.abstractActivity.startActivityForResult(intent, Constants.RQ_QR_SCANNER)
    }

    override fun unlockSession(pin: String) {
        if (sharePreferencesProvider.sharedPreferences().getString("pin", "") == pin) {
            sharePreferencesProvider.sharedPreferences().putBoolean("SessionLocked", false)
            view.startActivity(MainActivity::class.java, null, true, true, null)
        }
    }

    override fun onDestroy() {
        disposable.clear()
    }

    override fun logOut() {
        userManager?.let {
            disposable.add(it.d2.userModule().logOut()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                sharePreferencesProvider.sharedPreferences().putBoolean("SessionLocked", false)
                                view.handleLogout()
                            },
                            { view.handleLogout() }
                    )
            )
        }
    }

    override fun handleResponse(userResponse: Response<*>) {
        view.showLoginProgress(false)
        if (userResponse.isSuccessful) {
            sharePreferencesProvider.sharedPreferences().putBoolean(Preference.INITIAL_SYNC_DONE.name, false)
            (view.context.applicationContext as App).createUserComponent()
            view.saveUsersData()
        }
    }

    override fun handleError(throwable: Throwable) {
        Timber.e(throwable)
        if (throwable is D2Error && throwable.errorCode() == D2ErrorCode.ALREADY_AUTHENTICATED) {
            sharePreferencesProvider.sharedPreferences().putBoolean("SessionLocked", false)
            sharePreferencesProvider.sharedPreferences().putString("pin", null)
            view.alreadyAuthenticated()
        } else
            view.renderError(throwable)
        view.showLoginProgress(false)
    }

    //region FINGERPRINT
    override fun canHandleBiometrics(): Boolean? {
        return canHandleBiometrics
    }

    override fun onFingerprintClick() {
        view.showFingerprintDialog()
        disposable.add(
                goldfinger
                        .authenticate()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { credentials ->
                                    view.checkSecuredCredentials(credentials)
                                },
                                {
                                    view.displayMessage("AUTH ERROR")
                                    view.hideFingerprintDialog()
                                }))
    }

    override fun onAccountRecovery() {
        view.openAccountRecovery()
    }

    override fun onUrlInfoClick(v: View) {
        view.displayAlertDialog(R.string.login_server_info_title, R.string.login_server_info_message, null, R.string.action_accept)
    }
}