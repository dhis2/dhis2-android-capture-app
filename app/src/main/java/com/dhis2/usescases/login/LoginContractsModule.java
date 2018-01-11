package com.dhis2.usescases.login;


import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.View;

import com.dhis2.data.server.ConfigurationRepository;
import com.dhis2.databinding.ActivityLoginBinding;
import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.user.User;

import dagger.Module;
import dagger.Provides;
import retrofit2.Response;

@Module
public class LoginContractsModule {

    @Provides
    View provideView(LoginActivity loginActivity) {
        return loginActivity;
    }

    @Provides
    Presenter providePresenter(View view, ConfigurationRepository configurationRepository) {
        return new LoginPresenter(view, configurationRepository);
    }

    interface View extends AbstractActivityContracts.View {
        ActivityLoginBinding getBinding();

        @UiThread
        void showProgress();

        @UiThread
        void hideProgress();

        @UiThread
        void renderInvalidServerUrlError();

        @UiThread
        void renderInvalidCredentialsError();

        @UiThread
        void renderUnexpectedError();

        @UiThread
        void renderServerError();

        @UiThread
        void handleSync();

        @UiThread
        void onUnlockClick(android.view.View android);
    }

    interface Presenter {
        void onTextChanged(CharSequence s, int start, int before, int count);

        void onButtonClick();
    }

    interface Interactor {
        void validateCredentials(@NonNull String serverUrl,
                                 @NonNull String username, @NonNull String password);

        void sync();

        void handleResponse(@NonNull Response<User> userResponse);

        void handleError(@NonNull Throwable throwable);
    }

    interface Router {
        void navigateToHome();
    }

}