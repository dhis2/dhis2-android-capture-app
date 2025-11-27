package org.dhis2.usescases.general

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import org.dhis2.App
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.ActivityResultObservable
import org.dhis2.commons.ActivityResultObserver
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.commons.service.SessionManagerServiceImpl
import org.dhis2.commons.ui.extensions.handleInsets
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.server.OpenIdSession.LogOutReason
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.login.LoginActivity.Companion.bundle
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.usescases.splash.SplashActivity
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.FORGOT_CODE
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog
import javax.inject.Inject

abstract class SessionManagerActivity :
    AppCompatActivity(),
    ActivityResultObservable {
    @Inject
    lateinit var sessionManagerServiceImpl: SessionManagerServiceImpl

    @Inject
    lateinit var workManagerController: WorkManagerController

    @Inject
    lateinit var locationProvider: LocationProvider

    fun observableLifeCycle(): Observable<Status> = lifeCycleObservable

    open var handleEdgeToEdge = true

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private var pinDialog: PinDialog? = null

    private var lifeCycleObservable: BehaviorSubject<Status> =
        BehaviorSubject.create()

    var syncStatusController: SyncStatusController =
        SyncStatusController(
            object : DispatcherProvider {
                override fun io() = Dispatchers.IO

                override fun computation() = Dispatchers.Default

                override fun ui() = Dispatchers.Main
            },
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        val serverComponent = (applicationContext as App).serverComponent
        if (serverComponent != null) {
            serverComponent
                .openIdSession()
                .setSessionCallback(this) { logOutReason: LogOutReason? ->
                    startActivity(
                        LoginActivity::class.java,
                        bundle(true, -1, false, logOutReason),
                        true,
                        true,
                        null,
                    )
                    Unit
                }
            if (serverComponent.userManager().isUserLoggedIn().blockingFirst() &&
                !serverComponent.userManager().allowScreenShare()
            ) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE,
                )
            }
        }

        if (!resources.getBoolean(R.bool.is_tablet)) {
            requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }

        if (this is MainActivity || this is LoginActivity || this is SplashActivity) {
            serverComponent?.themeManager()?.clearProgramTheme()
        }

        if (this !is SplashActivity &&
            this !is LoginActivity &&
            this !is ScanActivity &&
            handleEdgeToEdge
        ) {
            if (serverComponent != null) {
                setTheme(serverComponent.themeManager().getProgramTheme())
            } else {
                setTheme(R.style.AppTheme)
            }
        }

        if (handleEdgeToEdge) handleInsets()

        super.onCreate(savedInstanceState)
    }

    override fun onUserInteraction() {
        if (::sessionManagerServiceImpl.isInitialized && this !is SplashActivity) sessionManagerServiceImpl.onUserInteraction()
    }

    private var comesFromImageSource: Boolean = false
    private var activityResultObserver: ActivityResultObserver? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (activityResultObserver != null) {
            activityResultObserver!!.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults,
            )
            activityResultObserver = null
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun subscribe(activityResultObserver: ActivityResultObserver) {
        this.activityResultObserver = activityResultObserver
    }

    private fun initPinDialog() {
        pinDialog =
            PinDialog(
                PinDialog.Mode.ASK,
                (this is LoginActivity),
                {
                    startActivity(MainActivity::class.java, null, true, true, null)
                    null
                },
                {
                    analyticsHelper.setEvent(FORGOT_CODE, CLICK, FORGOT_CODE)
                    if (this !is LoginActivity) {
                        startActivity(LoginActivity::class.java, null, true, true, null)
                    }
                    null
                },
            )
    }

    override fun unsubscribe() {
        this.activityResultObserver = null
    }

    override fun onPause() {
        super.onPause()
        lifeCycleObservable.onNext(Status.ON_PAUSE)
        if (::locationProvider.isInitialized) {
            locationProvider.stopLocationUpdates()
        }
    }

    fun startActivity(
        destination: Class<*>,
        bundle: Bundle?,
        finishCurrent: Boolean,
        finishAll: Boolean,
        transition: ActivityOptionsCompat?,
    ) {
        val intent = Intent(this, destination)
        if (finishAll) intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (bundle != null) intent.putExtras(bundle)
        if (transition != null) {
            startActivity(intent, transition.toBundle())
        } else {
            startActivity(intent, null)
        }
        if (finishCurrent) finish()
    }

    private fun showPinDialog() {
        pinDialog!!.show(supportFragmentManager, PIN_DIALOG_TAG)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        if (activityResultObserver != null && sessionManagerServiceImpl.isUserLoggedIn()) {
            comesFromImageSource = true
            activityResultObserver!!.onActivityResult(requestCode, resultCode, data)
            activityResultObserver = null
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkSessionTimeout() {
        if (::sessionManagerServiceImpl.isInitialized &&
            sessionManagerServiceImpl.checkSessionTimeout(
                { accountsCount -> sessionAction(accountsCount) },
                lifecycleScope,
            ) &&
            this !is LoginActivity
        ) {
            workManagerController.cancelAllWork()
            syncStatusController.restore()
        }
    }

    override fun onStop() {
        super.onStop()
        val dialog = pinDialog
        dialog?.dismissAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        lifeCycleObservable.onNext(Status.ON_RESUME)
        shouldCheckPIN()
    }

    enum class Status {
        ON_PAUSE,
        ON_RESUME,
    }

    private fun shouldCheckPIN() {
        if (comesFromImageSource) {
            this.app().disableBackGroundFlag()
            comesFromImageSource = false
        } else {
            if (this.app().isSessionBlocked && this !is SplashActivity && this !is LoginActivity) {
                if (pinDialog == null) {
                    initPinDialog()
                    showPinDialog()
                } else if (pinDialog?.isVisible == false) {
                    showPinDialog()
                }
            } else {
                if (this !is LoginActivity && this !is SplashActivity) {
                    checkSessionTimeout()
                }
            }
        }
    }

    private fun sessionAction(accountsCount: Int) {
        if (this.app().isSessionBlocked && this !is SplashActivity) {
            if (pinDialog == null) {
                initPinDialog()
                showPinDialog()
            }
        } else {
            navigateToLogin(accountsCount)
        }
    }

    private fun navigateToLogin(accountsCount: Int) {
        startActivity(
            LoginActivity::class.java,
            LoginActivity.bundle(
                accountsCount = accountsCount,
                isDeletion = false,
            ),
            true,
            true,
            null,
        )
    }
}
