package com.dhis2.usescases.login;


import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.dhis2.databinding.ActivityLoginBinding;
import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.common.D2ErrorCode;

import io.reactivex.functions.Consumer;
import retrofit2.Response;

public class LoginContracts {

    public interface View extends AbstractActivityContracts.View {
        ActivityLoginBinding getBinding();

        @UiThread
        void renderError(D2ErrorCode errorCode);

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
        void handleSync();

        @UiThread
        void onUnlockClick(android.view.View android);

        @UiThread
        void onLogoutClick(android.view.View android);

        @UiThread
        void setAutocompleteAdapters();

        @UiThread
        void saveUsersData();

        @NonNull
        Consumer<SyncResult> update(LoginActivity.SyncState syncState);

        void saveTheme(Integer themeId);

        void saveFlag(String s);

        void handleLogout();

        void setLoginVisibility(boolean isVisible);
    }

    public interface Presenter {
        void init(View view);

        void onTextChanged(CharSequence s, int start, int before, int count);

        void onButtonClick();

        void onQRClick(android.view.View v);

        ObservableField<Boolean> isServerUrlSet();

        ObservableField<Boolean> isUserNameSet();

        ObservableField<Boolean> isUserPassSet();

        void unlockSession(String pin);

        void onDestroy();

        void syncNext(LoginActivity.SyncState syncState, SyncResult syncResult);

        void logOut();

        void handleResponse(@NonNull Response userResponse);

        void handleError(@NonNull Throwable throwable);

        void sync();

        void syncEvents();

        void syncTrackedEntities();

        void syncReservedValues();

        void syncAggregatesData();

    }

    public interface Interactor {


    }

}