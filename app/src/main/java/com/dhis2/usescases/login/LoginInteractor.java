package com.dhis2.usescases.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.View;

import com.dhis2.App;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.server.ConfigurationRepository;
import com.dhis2.data.server.UserManager;
import com.dhis2.usescases.main.MainActivity;

import org.hisp.dhis.android.core.user.User;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import retrofit2.Response;
import rx.exceptions.OnErrorNotImplementedException;
import timber.log.Timber;

public class LoginInteractor implements LoginContracts.Interactor {

    private final MetadataRepository metadataRepository;
    private LoginContracts.View view;
    private ConfigurationRepository configurationRepository;
    private UserManager userManager;

    @NonNull
    private final CompositeDisposable disposable;

    LoginInteractor(LoginContracts.View view, ConfigurationRepository configurationRepository, MetadataRepository metadataRepository) {
        this.view = view;
        this.disposable = new CompositeDisposable();
        this.configurationRepository = configurationRepository;
        this.metadataRepository = metadataRepository;
        init();
    }

    private void init() {
        userManager = null;
        if (((App) view.getContext().getApplicationContext()).getServerComponent() != null)
            userManager = ((App) view.getContext().getApplicationContext()).getServerComponent().userManager();

        if (userManager != null) {
            disposable.add(userManager.isUserLoggedIn()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((isUserLoggedIn) -> {
                        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                                "com.dhis2", Context.MODE_PRIVATE);
                        if (isUserLoggedIn && !prefs.getBoolean("SessionLocked", false)) {
                            view.startActivity(MainActivity.class, null, true, true, null);
                        } else if (prefs.getBoolean("SessionLocked", false)) {
                            view.getBinding().unlock.setVisibility(View.VISIBLE);
                        }

                    }, Timber::e));
        }
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
                .switchMap((userManager) -> {
                    this.userManager = userManager;
                    return userManager.logIn(username, password);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        LoginInteractor.this::handleResponse
                        , LoginInteractor.this::handleError));
    }

    @Override
    public void sync() {

        disposable.add(metadata()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(
                        update(LoginActivity.SyncState.METADATA),
                        throwable -> {
                            throw new OnErrorNotImplementedException(throwable);
                        }));

    }

    @Override
    public void syncEvents() {
        disposable.add(
                metadataRepository.getTheme()
                        .flatMap(flagTheme -> {
                            view.saveFlag(flagTheme.val0());
                            view.saveTheme(flagTheme.val1());
                            return events();
                        })
                        .subscribeOn(Schedulers.io())
                        .map(response -> SyncResult.success())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn(throwable -> SyncResult.failure(
                                throwable.getMessage() == null ? "" : throwable.getMessage()))
                        .startWith(SyncResult.progress())
                        .subscribe(update(LoginActivity.SyncState.EVENTS),
                                throwable -> view.displayMessage(throwable.getMessage())
                        ));
    }

    @Override
    public void syncTrackedEntities() {

        disposable.add(trackerData()
                .subscribeOn(Schedulers.io())
                .map(response -> SyncResult.success())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(LoginActivity.SyncState.TEI),
                        throwable -> view.displayMessage(throwable.getMessage())
                ));

    }

    @NonNull
    private Observable<Response> metadata() {
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().syncMetaData()));
    }

    @NonNull
    private Observable<Response> trackerData() {
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().downloadTrackedEntityInstances(50)));
    }


    @NonNull
    private Observable<Response> events() {
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().syncSingleData(100)));
    }

    @NonNull
    private Consumer<SyncResult> update(LoginActivity.SyncState syncState) {
        return result -> {
            if (view != null) {
                view.update(syncState).accept(result);
            }
        };
    }


    @Override
    public void handleResponse(@NonNull Response<User> userResponse) {
        Timber.d("Authentication response url: %s", userResponse.raw().request().url().toString());
        Timber.d("Authentication response code: %s", userResponse.code());
        if (userResponse.isSuccessful()) {
            ((App) view.getContext().getApplicationContext()).createUserComponent();
            view.saveUsersData();
            view.handleSync();
            sync();
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

    @Override
    public void onDestroy() {
        disposable.clear();
    }
}