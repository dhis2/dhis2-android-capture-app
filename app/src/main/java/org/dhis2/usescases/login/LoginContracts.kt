package org.dhis2.usescases.login


import android.view.View
import androidx.annotation.UiThread
import org.dhis2.data.sharedPreferences.SharePreferencesProvider
import org.dhis2.usescases.general.AbstractActivityContracts
import retrofit2.Response

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
        fun saveUsersData()

        fun handleLogout()

        fun setLoginVisibility(isVisible: Boolean)

        fun showLoginProgress(showLogin: Boolean)

        fun goToNextScreen()

        fun setUrl(url: String)

        fun setUser(user: String)

        fun showCrashlyticsDialog()

        @UiThread
        fun renderError(throwable: Throwable)

        //FingerPrintAuth

        fun showBiometricButton()

        fun checkSecuredCredentials()

        fun openAccountRecovery()

        fun displayAlertDialog(titleResource:Int, descriptionResource:Int,negativeResource:Int?,positiveResource:Int)
        fun alreadyAuthenticated()
        fun setPreference(sharePreferencesProvider: SharePreferencesProvider)
    }

    interface Presenter {
        fun init(view: View)

        fun logIn(serverUrl: String, userName: String, pass: String)

        fun onQRClick(v: android.view.View)

        fun unlockSession(pin: String)

        fun logOut()

        fun onButtonClick()

        fun onDestroy()

        fun handleResponse(userResponse: Response<*>)

        fun handleError(throwable: Throwable)

        fun onAccountRecovery()

        //FingerPrintAuth

        fun onFingerprintClick()

        fun canHandleBiometrics(): Boolean?
        fun onUrlInfoClick(v: android.view.View)
    }

}