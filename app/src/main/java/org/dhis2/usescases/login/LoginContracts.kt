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

        @UiThread
        fun saveUsersData(
            displayTrackingMessage: Boolean,
            isInitialSyncDone: Boolean,
        )

        fun handleLogout()

        fun goToNextScreen()

        @UiThread
        fun renderError(throwable: Throwable)

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
