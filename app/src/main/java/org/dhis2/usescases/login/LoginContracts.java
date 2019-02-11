package org.dhis2.usescases.login;


import org.dhis2.databinding.ActivityLoginBinding;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.databinding.ObservableField;
import retrofit2.Response;

public class LoginContracts {

    public interface View extends AbstractActivityContracts.View {
        ActivityLoginBinding getBinding();

        @UiThread
        void renderError(D2ErrorCode errorCode, String defaultMessage);

        @UiThread
        void renderInvalidServerUrlError();

        @UiThread
        void renderUnexpectedError();

        @UiThread
        void onUnlockClick(android.view.View android);

        @UiThread
        void onLogoutClick(android.view.View android);

        @UiThread
        void setAutocompleteAdapters();

        @UiThread
        void saveUsersData();

        void handleLogout();

        void setLoginVisibility(boolean isVisible);

        void setTestingCredentials();

        void resetCredentials(boolean resetServer, boolean resetUser, boolean resetPass);

        void showLoginProgress(boolean showLogin);

        void showBiometricButton();

        void checkSecuredCredentials();

        void goToNextScreen();

        void switchPasswordVisibility();

        void setUrl(String url);
    }

    public interface Presenter {
        void init(View view);

        void onServerChanged(CharSequence s, int start, int before, int count);

        void onUserChanged(CharSequence s, int start, int before, int count);

        void onPassChanged(CharSequence s, int start, int before, int count);

        void onButtonClick();

        void logIn(String serverUrl, String userName, String pass);

        void onQRClick(android.view.View v);

        void onVisibilityClick(android.view.View v);

        ObservableField<Boolean> isServerUrlSet();

        ObservableField<Boolean> isUserNameSet();

        ObservableField<Boolean> isUserPassSet();

        void unlockSession(String pin);

        void onDestroy();


        void logOut();

        void handleResponse(@NonNull Response userResponse);

        void handleError(@NonNull Throwable throwable);

        void onFingerprintClick();

        Boolean canHandleBiometrics();

    }

}