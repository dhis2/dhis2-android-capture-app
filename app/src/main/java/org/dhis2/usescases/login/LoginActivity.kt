package org.dhis2.usescases.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import org.dhis2.App
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.bindings.buildInfo
import org.dhis2.commons.Constants.SESSION_DIALOG_RQ
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.server.OpenIdSession
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.screen.LoginScreen
import org.dhis2.usescases.about.PolicyView
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.auth.OpenIdProviders
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.sync.SyncActivity
import org.dhis2.utils.NetworkUtils
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.FORGOT_CODE
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.koin.android.ext.android.inject
import javax.inject.Inject

const val EXTRA_SKIP_SYNC = "SKIP_SYNC"
const val EXTRA_SESSION_EXPIRED = "EXTRA_SESSION_EXPIRED"
const val EXTRA_ACCOUNT_DISABLED = "EXTRA_ACCOUNT_DISABLED"
const val IS_DELETION = "IS_DELETION"
const val ACCOUNTS_COUNT = "ACCOUNTS_COUNT"
const val FROM_SPLASH = "FROM_SPLASH"

class LoginActivity :
    ActivityGlobalAbstract(),
    LoginContracts.View {
    override var handleEdgeToEdge = false

    @Inject
    lateinit var presenter: LoginViewModel

    @Inject
    lateinit var openIdProviders: OpenIdProviders

    @Inject
    lateinit var resourceManager: ResourceManager

    private val appLinkNavigation: AppLinkNavigation by inject()

    private var isPinScreenVisible = false
    private var qrUrl: String? = null

    private var skipSync = false
    private var openIDRequestCode = -1

    companion object {
        fun bundle(
            skipSync: Boolean = false,
            accountsCount: Int = -1,
            isDeletion: Boolean = false,
            logOutReason: OpenIdSession.LogOutReason? = null,
            fromSplash: Boolean = false,
        ): Bundle =
            Bundle().apply {
                putBoolean(EXTRA_SKIP_SYNC, skipSync)
                putBoolean(IS_DELETION, isDeletion)
                putInt(ACCOUNTS_COUNT, accountsCount)
                putBoolean(FROM_SPLASH, fromSplash)
                when (logOutReason) {
                    OpenIdSession.LogOutReason.OPEN_ID -> putBoolean(EXTRA_SESSION_EXPIRED, true)
                    OpenIdSession.LogOutReason.DISABLED_ACCOUNT ->
                        putBoolean(
                            EXTRA_ACCOUNT_DISABLED,
                            true,
                        )

                    null -> {
                        // Nothing to do in this case
                    }
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(SurfaceColor.Primary.toArgb()),
        )
        val loginComponent =
            app().loginComponent() ?: app().createLoginComponent(
                LoginModule(
                    view = this,
                    viewModelStoreOwner = this,
                    userManager = app().serverComponent?.userManager(),
                ),
            )

        loginComponent.inject(this)

        super.onCreate(savedInstanceState)

        checkMessage()

        skipSync = intent.getBooleanExtra(EXTRA_SKIP_SYNC, false)

        setContent {
            DHIS2Theme {
                LoginScreen(
                    versionName = buildInfo(),
                    onNavigateToHome = {
                        app().createUserComponent()
                        startActivity(MainActivity::class.java, null, true, true, null)
                    },
                    onNavigateToSync = {
                        app().createUserComponent()
                        startActivity(SyncActivity::class.java, null, true, true, null)
                    },
                    onNavigateToPrivacyPolicy = {
                        activity?.let {
                            startActivity(Intent(it, PolicyView::class.java))
                        }
                    },
                    onFinish = {
                        finish()
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val appLinkAction = intent?.action
        val appLinkData: Uri? = intent?.data
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            appLinkNavigation.appLink.tryEmit(appLinkData.toString())
            intent?.action = null
        }
    }

    override fun onPause() {
        presenter.onDestroy()
        super.onPause()
    }

    override fun onDestroy() {
        (applicationContext as App).releaseLoginComponent()
        super.onDestroy()
    }

    override fun goToNextScreen() {
        if (isNetworkAvailable() && !skipSync) {
            startActivity(SyncActivity::class.java, null, true, true, null)
        } else {
            startActivity(MainActivity::class.java, null, true, true, null)
        }
    }

    override fun isNetworkAvailable(): Boolean = NetworkUtils.isOnline(this)

    override fun handleLogout() {
        recreate()
    }

    override fun alreadyAuthenticated() {
        startActivity(MainActivity::class.java, null, true, true, null)
    }

    override fun onUnlockClick() {
        PinDialog(
            PinDialog.Mode.ASK,
            false,
            {
                startActivity(MainActivity::class.java, null, true, true, null)
            },
            {
                analyticsHelper.setEvent(FORGOT_CODE, CLICK, FORGOT_CODE)
            },
        ).show(supportFragmentManager, PIN_DIALOG_TAG)
    }

    override fun onLogoutClick(android: View) {
        presenter.logOut()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isPinScreenVisible) {
            isPinScreenVisible = false
        } else {
            super.onBackPressed()
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        if (requestCode == openIDRequestCode && resultCode == Activity.RESULT_OK) {
            data?.let {
                presenter.handleAuthResponseData(
                    "server_url", // TODO ("Move this to login module and pass server url")
                    data,
                    requestCode,
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun initLogin(): UserManager = app().createServerComponent().userManager()

    override fun openOpenIDActivity(intentData: IntentWithRequestCode?) {
        intentData?.let {
            openIDRequestCode = intentData.requestCode
            startActivityForResult(intentData.intent, intentData.requestCode)
        }
    }

    private fun checkMessage() {
        if (intent.getBooleanExtra(EXTRA_SESSION_EXPIRED, false)) {
            showSessionExpired()
        } else if (intent.getBooleanExtra(EXTRA_ACCOUNT_DISABLED, false)) {
            showAccountDisabled()
        }
    }

    private fun showSessionExpired() {
        val sessionDialog =
            CustomDialog(
                this,
                getString(R.string.openid_session_expired),
                getString(R.string.openid_session_expired_message),
                getString(R.string.action_accept),
                null,
                SESSION_DIALOG_RQ,
                null,
            )
        sessionDialog.setCancelable(false)
        sessionDialog.show()
    }

    private fun showAccountDisabled() {
        val sessionDialog =
            CustomDialog(
                this,
                getString(R.string.account_disable_title),
                getString(R.string.account_disable_message),
                getString(R.string.action_accept),
                null,
                SESSION_DIALOG_RQ,
                null,
            )
        sessionDialog.setCancelable(false)
        sessionDialog.show()
    }

    override fun showNoConnectionDialog() {
        val dialog =
            CustomDialog(
                this,
                getString(R.string.network_unavailable),
                getString(R.string.no_network_to_recover_account),
                getString(R.string.action_ok),
                null,
                CustomDialog.NO_RQ_CODE,
                null,
            )
        dialog.show()
    }
}
