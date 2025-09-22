package org.dhis2.usescases.login

import androidx.annotation.UiThread
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode

class LoginContracts {
    interface View : AbstractActivityContracts.View {
        @UiThread
        fun onUnlockClick()

        @UiThread
        fun onLogoutClick(android: android.view.View)

        fun handleLogout()

        fun goToNextScreen()

        // FingerPrintAuth

        fun openAccountRecovery()

        fun alreadyAuthenticated()

        fun getDefaultServerProtocol(): String

        fun isNetworkAvailable(): Boolean

        fun openOpenIDActivity(it: IntentWithRequestCode?)

        fun showNoConnectionDialog()

        fun initLogin(): UserManager?
    }
}
