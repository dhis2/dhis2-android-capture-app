package org.dhis2.usescases.login

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.core.app.ActivityCompat
import com.github.pwittchen.rxbiometric.library.RxBiometric
import com.github.pwittchen.rxbiometric.library.validation.RxPreconditions
import de.adorsys.android.securestoragelibrary.SecurePreferences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.server.ConfigurationRepository
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.qrScanner.QRActivity
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import retrofit2.Response
import timber.log.Timber

class LoginPresenter internal constructor(private val configurationRepository: ConfigurationRepository) : LoginContracts.Presenter {

    private lateinit var view: LoginContracts.View
    private var userManager: UserManager? = null
    private lateinit var disposable: CompositeDisposable

    private var canHandleBiometrics: Boolean? = null

    override fun init(view: LoginContracts.View) {
        this.view = view
        this.disposable = CompositeDisposable()

        if ((view.context.applicationContext as App).serverComponent != null)
            userManager = (view.context.applicationContext as App).serverComponent.userManager()

        userManager?.let { userManager ->
            disposable.add(userManager.isUserLoggedIn
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ isUserLoggedIn ->
                        val prefs = view.abstracContext.getSharedPreferences(
                                Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                        if (isUserLoggedIn && !prefs.getBoolean("SessionLocked", false)) {
                            view.startActivity(MainActivity::class.java, null, true, true, null)
                        } else if (prefs.getBoolean("SessionLocked", false)) {
                            view.showUnlockButton()
                        }

                    }, { Timber.e(it) }))

            disposable.add(
                    Observable.just(if (userManager.d2.systemInfoModule().systemInfo.get() != null)
                        userManager.d2.systemInfoModule().systemInfo.get()
                    else
                        SystemInfo.builder().build())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { systemInfo ->
                                        if (systemInfo.contextPath() != null) {
                                            val prefs = view.abstractActivity.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                                            view.setUrl(systemInfo.contextPath() ?: "")
                                            view.setUser(prefs.getString(Constants.USER, "")!!)
                                        } else
                                            view.setUrl(view.context.getString(R.string.login_https))
                                    },
                                    { Timber.e(it) }))
        } ?: view.setUrl(view.context.getString(R.string.login_https))


        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        //TODO: REMOVE FALSE WHEN GREEN LIGHT
            disposable.add(RxPreconditions
                    .hasBiometricSupport(view.context)
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
        val prefs = view.abstracContext.getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(Constants.USER_ASKED_CRASHLYTICS, false))
            view.showCrashlyticsDialog()
        else
            view.showLoginProgress(true)
    }

    override fun logIn(serverUrl: String, userName: String, pass: String) {
        val baseUrl = HttpUrl.parse(canonizeUrl(serverUrl+"/api")) ?: return
        disposable.add(
                configurationRepository.configure(baseUrl)
                        .map { config -> (view.abstractActivity.applicationContext as App).createServerComponent(config).userManager() }
                        .switchMap { userManager ->
                            val prefs = view.abstractActivity.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                            prefs.edit().putString(Constants.SERVER, serverUrl).apply()
                            this.userManager = userManager
                            userManager.logIn(userName.trim { it <= ' ' }, pass).map<Response<Any>> { user ->
                                if (user == null)
                                    Response.error<Any>(404, ResponseBody.create(MediaType.parse("text"), "NOT FOUND"))
                                else {
                                    prefs.edit().putString(Constants.USER, user.userCredentials()?.username()).apply()
                                    prefs.edit().putBoolean("SessionLocked", false).apply()
                                    prefs.edit().putString("pin", null).apply()
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

    private fun canonizeUrl(serverUrl: String): String {
        var urlToCanonized = serverUrl.trim { it <= ' ' }
        urlToCanonized = urlToCanonized.replace(" ", "")
        return if (urlToCanonized.endsWith("/")) urlToCanonized else "$urlToCanonized/"
    }

    override fun onQRClick(v: View) {
        val intent = Intent(view.context, QRActivity::class.java)
        view.abstractActivity.startActivityForResult(intent, Constants.RQ_QR_SCANNER)
    }

    override fun unlockSession(pin: String) {
        val prefs = view.abstracContext.getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE)
        if (prefs.getString("pin", "") == pin) {
            prefs.edit().putBoolean("SessionLocked", false).apply()
            view.startActivity(MainActivity::class.java, null, true, true, null)
        }
    }

    override fun onDestroy() {
        disposable.clear()
    }


    override fun logOut() {
        userManager?.let {
            disposable.add(Observable.fromCallable<org.hisp.dhis.android.core.common.Unit>(it.d2.userModule().logOut())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { data ->
                                val prefs = view.abstracContext.sharedPreferences
                                prefs.edit().putBoolean("SessionLocked", false).apply()
                                view.handleLogout()
                            },
                            { t -> view.handleLogout() }
                    )
            )
        }
    }

    override fun handleResponse(userResponse: Response<*>) {
        view.showLoginProgress(false)
        if (userResponse.isSuccessful) {
            (view.context.applicationContext as App).createUserComponent()
            view.saveUsersData()
        }
    }

    override fun handleError(throwable: Throwable) {
        Timber.e(throwable)
        if (throwable is D2Error && throwable.errorCode() == D2ErrorCode.ALREADY_AUTHENTICATED) {
            val prefs = view.abstractActivity.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)
            prefs.edit().putBoolean("SessionLocked", false).apply()
            prefs.edit().putString("pin", null).apply()
            view.alreadyAuthenticated()
//            handleResponse(Response.success<Any>(null))
        }else
            view.renderError(throwable)
        view.showLoginProgress(false)
    }

    //region FINGERPRINT
    override fun canHandleBiometrics(): Boolean? {
        return canHandleBiometrics
    }

    override fun onFingerprintClick() {
        disposable.add(
                RxBiometric
                        .title("Title")
                        .description("description")
                        .negativeButtonText("Cancel")
                        .negativeButtonListener(DialogInterface.OnClickListener { dialogInterface, i -> })
                        .executor(ActivityCompat.getMainExecutor(view.abstractActivity))
                        .build()
                        .authenticate(view.abstractActivity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.checkSecuredCredentials() },
                                { error -> view.displayMessage("AUTH ERROR") }))
    }

    override fun onAccountRecovery() {
        view.openAccountRecovery()
    }

    override fun onUrlInfoClick(v: View) {
        view.displayAlertDialog(R.string.login_server_info_title, R.string.login_server_info_message, null, R.string.action_accept)
    }
}