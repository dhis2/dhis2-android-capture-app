package org.dhis2.usescases.login;


import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import org.dhis2.databinding.ActivityLoginBinding;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.common.D2ErrorCode;

import retrofit2.Response;

public class LoginContracts {

    public interface View extends AbstractActivityContracts.View {
        ActivityLoginBinding getBinding();

        @UiThread
        void renderError(D2ErrorCode errorCode, String defaultMessage);

        @UiThread
        void renderInvalidServerUrlError();

        @UiThread
        void renderInvalidCredentialsError();

        @UiThread
        void renderUnexpectedError();

        @UiThread
        void renderEmptyUsername();

        @UiThread
        void renderEmptyPassword();

        @UiThread
        void renderServerError();

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
    }

    public interface Presenter {
        void init(View view);

        void onTextChanged(CharSequence s, int start, int before, int count);

        void onButtonClick();

        void onTestingEnvironmentClick(int dhisVersion);

        void onQRClick(android.view.View v);

        ObservableField<Boolean> isServerUrlSet();

        ObservableField<Boolean> isUserNameSet();

        ObservableField<Boolean> isUserPassSet();

        void unlockSession(String pin);

        void onDestroy();


        void logOut();

        void handleResponse(@NonNull Response userResponse);

        void handleError(@NonNull Throwable throwable);

    }

    public interface Interactor {


    }

}