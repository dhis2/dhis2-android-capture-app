package org.dhis2.usescases.login

import androidx.annotation.UiThread
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode

class LoginContracts {

    interface View : AbstractActivityContracts.View {
        @UiThread
        fun showUnlockButton()

        @UiThread
        fun onUnlockClick(android: android.view.View)

        @UiThread
        fun onLogoutClick(android: android.view.View)

        @UiThread
        fun setAutocompleteAdapters()

        @UiThread
        fun saveUsersData(displayTrackingMessage: Boolean, isInitialSyncDone: Boolean)

        fun handleLogout()

        fun setLoginVisibility(isVisible: Boolean)

        fun goToNextScreen()

        fun setUrl(url: String?)

        fun setUser(user: String?)

        fun navigateToQRActivity()

        @UiThread
        fun renderError(throwable: Throwable)

        // FingerPrintAuth

        fun showBiometricButton()

        fun openAccountRecovery()

        fun alreadyAuthenticated()
        fun showCredentialsData(vararg args: String)
        fun showEmptyCredentialsMessage()
        fun getDefaultServerProtocol(): String
        fun isNetworkAvailable(): Boolean
        fun openOpenIDActivity(it: IntentWithRequestCode?)
        fun openAccountsActivity()
        fun showNoConnectionDialog()
        fun initLogin(): UserManager?
        fun onDbImportFinished(isSuccess: Boolean)
    }
}
