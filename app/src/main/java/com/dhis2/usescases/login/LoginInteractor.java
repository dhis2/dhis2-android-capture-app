package com.dhis2.usescases.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.data.server.ConfigurationRepository;
import com.dhis2.service.SyncService;
import com.dhis2.App;

import org.hisp.dhis.android.core.user.User;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import retrofit2.Response;
import timber.log.Timber;

public class LoginInteractor implements LoginContractsModule.Interactor {

    private LoginContractsModule.View view;
    private LoginContractsModule.Router router;
    private ConfigurationRepository configurationRepository;

    @NonNull
    private final CompositeDisposable disposable;

    LoginInteractor(LoginContractsModule.View view, ConfigurationRepository configurationRepository) {
        this.view = view;
        this.router = new LoginRouter(view);
        this.disposable = new CompositeDisposable();
        this.configurationRepository = configurationRepository;
    }

    @UiThread
    @Override
    public void validateCredentials(@NonNull String serverUrl,
                                    @NonNull String username, @NonNull String password) {
        HttpUrl baseUrl = HttpUrl.parse(canonizeUrl(serverUrl));
        if (baseUrl == null) {
            return;
        }

        disposable.add(configurationRepository.configure(baseUrl)
                .map((config) -> ((App) view.getContext().getApplicationContext()).createServerComponent(config).userManager())
                .switchMap((userManager) -> userManager.logIn(username, password))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(LoginInteractor.this::handleResponse, LoginInteractor.this::handleError));
    }

    @Override
    public void sync() {
        view.getContext().startService(new Intent(view.getContext().getApplicationContext(),SyncService.class));
    }

    @Override
    public void handleResponse(@NonNull Response<User> userResponse) {
        Timber.d("Authentication response url: %s", userResponse.raw().request().url().toString());
        Timber.d("Authentication response code: %s", userResponse.code());
        if (userResponse.isSuccessful()) {
            router.navigateToHome();
        } else if (userResponse.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            view.hideProgress();
            view.renderInvalidCredentialsError();
        } else if (userResponse.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            view.hideProgress();
            view.renderInvalidCredentialsError();
        } else if (userResponse.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
            view.hideProgress();
            view.renderUnexpectedError();
        } else if (userResponse.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            view.hideProgress();
            view.renderServerError();
        }
    }

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Timber.e(throwable);

        if (throwable instanceof IOException) {
            view.hideProgress();
            view.renderInvalidServerUrlError();
        } else {
            view.hideProgress();
            view.renderUnexpectedError();
        }
    }

    private String canonizeUrl(@NonNull String serverUrl) {
        return serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";
    }

}