package org.dhis2.usescases.login;


import org.dhis2.usescases.general.AbstractActivityContracts;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import retrofit2.Response;

public class LoginContracts {

    public interface View extends AbstractActivityContracts.View {
        @UiThread
        void showUnlockButton();

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

        void showLoginProgress(boolean showLogin);

        void goToNextScreen();

        void switchPasswordVisibility();

        void setUrl(String url);

        void showCrashlyticsDialog();

        @UiThread
        void renderError(Throwable throwable);

        //FingerPrintAuth

        void showBiometricButton();

        void checkSecuredCredentials();
    }

    public interface Presenter {
        void init(View view);

        void logIn(String serverUrl, String userName, String pass);

        void onQRClick(android.view.View v);

        void onVisibilityClick(android.view.View v);

        void unlockSession(String pin);

        void logOut();

        void onButtonClick();

        void onDestroy();

        void handleResponse(@NonNull Response userResponse);

        void handleError(@NonNull Throwable throwable);

        //FingerPrintAuth

        void onFingerprintClick();

        Boolean canHandleBiometrics();

    }

}