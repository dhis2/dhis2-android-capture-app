package org.dhis2.usescases.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.bindings.buildInfo
import org.dhis2.commons.Constants.SESSION_DIALOG_RQ
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.server.OpenIdSession
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.screen.LoginScreen
import org.dhis2.usescases.about.PolicyView
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.sync.SyncActivity
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.koin.android.ext.android.inject
import javax.inject.Inject

const val EXTRA_SKIP_SYNC = "SKIP_SYNC"
const val FROM_MAIN_ACTIVITY = "FROM_MAIN_ACTIVITY"
const val EXTRA_SESSION_EXPIRED = "EXTRA_SESSION_EXPIRED"
const val EXTRA_ACCOUNT_DISABLED = "EXTRA_ACCOUNT_DISABLED"
const val IS_DELETION = "IS_DELETION"
const val ACCOUNTS_COUNT = "ACCOUNTS_COUNT"
const val FROM_SPLASH = "FROM_SPLASH"

class LoginActivity : ActivityGlobalAbstract() {
    override var handleEdgeToEdge = false

    @Inject
    lateinit var resourceManager: ResourceManager

    private val appLinkNavigation: AppLinkNavigation by inject()

    private var isPinScreenVisible = false

    private var skipSync = false
    private var fromHome = false

    companion object {
        fun bundle(
            skipSync: Boolean = false,
            accountsCount: Int = -1,
            isDeletion: Boolean = false,
            logOutReason: OpenIdSession.LogOutReason? = null,
            fromSplash: Boolean = false,
            fromMainActivity: Boolean = false,
        ): Bundle =
            Bundle().apply {
                putBoolean(EXTRA_SKIP_SYNC, skipSync)
                putBoolean(FROM_MAIN_ACTIVITY, fromMainActivity)
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
        super.onCreate(savedInstanceState)

        checkMessage()

        skipSync = intent.getBooleanExtra(EXTRA_SKIP_SYNC, false)
        fromHome = intent.getBooleanExtra(FROM_MAIN_ACTIVITY, false)

        setContent {
            DHIS2Theme {
                LoginScreen(
                    versionName = buildInfo(),
                    fromHome = fromHome,
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isPinScreenVisible) {
            isPinScreenVisible = false
        } else {
            super.onBackPressed()
            finish()
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
}
