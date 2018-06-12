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
import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.main.MainActivity;
import com.dhis2.utils.Constants;

import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

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

    final String KEYSTORE_PROVIDER = "AndroidKeyStore";

    private final MetadataRepository metadataRepository;
    private LoginContracts.View view;
    private ConfigurationRepository configurationRepository;
    private UserManager userManager;

    @NonNull
    private final CompositeDisposable disposable;
    private KeyStore mStore;

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

        /*if (prefStore.contains("user_credentials") &&
                prefStore.getString("user_credentials", null).equals(String.format("%s:%s", username, password))) {

            if (NetworkUtils.isOnline(view.getContext()))
                handleResponse(Response.success(null));
            else
                view.startActivity(MainActivity.class, null, true, true, null);


        } else*/
        disposable.add(configurationRepository.configure(baseUrl)
                .map(config -> ((App) view.getAbstractActivity().getApplicationContext()).createServerComponent(config).userManager())
                .switchMap((userManager) -> {
                    SharedPreferences prefs = view.getAbstractActivity().getSharedPreferences(
                            "com.dhis2", Context.MODE_PRIVATE);
//                    prefs.edit().putString("SERVER", serverUrl).apply();
                    this.userManager = userManager;
                    return userManager.logIn(username, password);
                })
                .map(user -> {
                    if (user == null)
                        return Response.error(404, null);
                    else {
                        saveUserData(username, password);
                        return Response.success(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        LoginInteractor.this::handleResponse
                        , LoginInteractor.this::handleError));
    }

    private void saveUserData(String username, String password) {
        //prefStore.edit().putString("user_credentials", String.format("%s:%s", username, password)).commit();
    }

    void loadKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        mStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        mStore.load(null);
    }

    @Override
    public void sync() {

        disposable.add(
                Observable.just(true)
                        .map(response -> {
                            userManager.getD2().syncMetaData().call();
                            return SyncResult.success();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn(throwable -> SyncResult.failure(
                                throwable.getMessage() == null ? "" : throwable.getMessage()))
                        .startWith(SyncResult.progress())
                        .subscribe(
                                update(LoginActivity.SyncState.METADATA),
                                throwable -> {
                                    throw new OnErrorNotImplementedException(throwable);
                                }));

       /* disposable.add(metadata()
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
                        }));*/

    }

    @Override
    public void syncEvents() {
        disposable.add(
                metadataRepository.getTheme()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(flagTheme -> {
                                    view.saveFlag(flagTheme.val0());
                                    view.saveTheme(flagTheme.val1());
                                },
                                Timber::e
                        ));

        disposable.add(events()
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

    @Override
    public void syncReservedValues() {
        disposable.add(metadataRepository.getReserveUids()
                .map(pairs -> {
                    for (Pair<String, String> pair : pairs) {
                        userManager.getD2().popTrackedEntityAttributeReservedValue(pair.val0(), pair.val1());
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        data -> Timber.log(1, "DONE"),
                        Timber::d
                )
        );
    }

    @NonNull
    private Observable<Void> metadata() {
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().syncMetaData()));
    }

    @NonNull
    private Observable<List<TrackedEntityInstance>> trackerData() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        int teiLimit = prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        boolean limityByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().downloadTrackedEntityInstances(teiLimit, limityByOU)));
    }


    @NonNull
    private Observable<List<Event>> events() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        int eventLimit = prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        boolean limityByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);

        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().downloadSingleEvents(eventLimit, limityByOU)));
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
    public void handleResponse(@NonNull Response userResponse) {
        Timber.d("Authentication response url: %s", userResponse.raw().request().url().toString());
        Timber.d("Authentication response code: %s", userResponse.code());
        if (userResponse.isSuccessful()) {
            ((App) view.getContext().getApplicationContext()).createUserComponent();
            view.saveUsersData();
            view.handleSync();
            sync();
        } else if (userResponse.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            view.renderInvalidCredentialsError();
        } else if (userResponse.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            view.renderInvalidCredentialsError();
        } else if (userResponse.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
            view.renderUnexpectedError();
        } else if (userResponse.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            view.renderServerError();
        }
    }

    /*@Override
    public void handleResponse(@NonNull Response<User> userResponse) {
        Timber.d("Authentication response url: %s", userResponse.raw().request().url().toString());
        Timber.d("Authentication response code: %s", userResponse.code());
        if (userResponse.isSuccessful()) {
            ((App) view.getContext().getApplicationContext()).createUserComponent();
            view.saveUsersData();
            view.handleSync();
            sync();
        } else if (userResponse.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            view.renderInvalidCredentialsError();
        } else if (userResponse.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            view.renderInvalidCredentialsError();
        } else if (userResponse.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
            view.renderUnexpectedError();
        } else if (userResponse.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            view.renderServerError();
        }
    }*/

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Timber.e(throwable);

        if (throwable instanceof IOException) {
            view.renderInvalidServerUrlError();
        } else {
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

    @Override
    public void logOut() {
        if (userManager != null)
            disposable.add(Observable.fromCallable(userManager.getD2().logout())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                            data -> view.handleLogout(),
                            Timber::d
                    )
            );
    }
}