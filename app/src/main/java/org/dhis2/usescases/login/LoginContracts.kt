package org.dhis2.usescases.login


import android.view.View
import androidx.annotation.UiThread
import co.infinum.goldfinger.Goldfinger
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.D2
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

        fun showFingerprintDialog()

        fun hideFingerprintDialog()

        @UiThread
        fun renderError(throwable: Throwable)

        //FingerPrintAuth

        fun showBiometricButton()

        fun openAccountRecovery()

        fun displayAlertDialog(titleResource:Int, descriptionResource:Int,negativeResource:Int?,positiveResource:Int)
        fun alreadyAuthenticated()
        fun showCredentialsData(type: Goldfinger.Type, vararg args: String)
        fun showEmptyCredentialsMessage()
    }

    interface Presenter {
        fun init(view: View)

        fun logIn(serverUrl: String, userName: String, pass: String)

        fun onQRClick(v: android.view.View)

        fun unlockSession(pin: String)

        fun stopReadingFingerprint()

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