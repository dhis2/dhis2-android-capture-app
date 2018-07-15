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
import com.dhis2.data.service.SyncMetadataService;
import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.main.MainActivity;
import com.dhis2.utils.Constants;
import com.dhis2.utils.NetworkUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import org.hisp.dhis.android.core.common.D2CallException;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.io.IOException;
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

import static com.dhis2.utils.Constants.TIME_DAILY;

public class LoginInteractor implements LoginContracts.Interactor {

    private final MetadataRepository metadataRepository;
    private LoginContracts.View view;
    private ConfigurationRepository configurationRepository;
    private UserManager userManager;
    private FirebaseJobDispatcher dispatcher;

    @NonNull
    private final CompositeDisposable disposable;

    LoginInteractor(LoginContracts.View view, ConfigurationRepository configurationRepository, MetadataRepository metadataRepository, FirebaseJobDispatcher firebaseJobDispatcher) {
        this.view = view;
        this.disposable = new CompositeDisposable();
        this.configurationRepository = configurationRepository;
        this.metadataRepository = metadataRepository;
        this.dispatcher = firebaseJobDispatcher;
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
                    .subscribe(isUserLoggedIn -> {
                        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                                "com.dhis2", Context.MODE_PRIVATE);
                        if (isUserLoggedIn && !prefs.getBoolean("SessionLocked", false)) {
                            view.startActivity(MainActivity.class, null, true, true, null);
                        } else if (prefs.getBoolean("SessionLocked", false)) {
                            view.getBinding().unlockLayout.setVisibility(View.VISIBLE);
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
                .map(config -> ((App) view.getAbstractActivity().getApplicationContext()).createServerComponent(config).userManager())
                .switchMap(userManager -> {
                    SharedPreferences prefs = view.getAbstractActivity().getSharedPreferences(
                            "com.dhis2", Context.MODE_PRIVATE);
                    prefs.edit().putString("SERVER", serverUrl).apply();
                    this.userManager = userManager;
                    return userManager.logIn(username, password);
                })
                .map(user -> {
                    if (user == null)
                        return Response.error(404, null);
                    else {
                        return Response.success(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        LoginInteractor.this::handleResponse,
                        LoginInteractor.this::handleError));
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
                .map(response -> {
//                    userManager.getD2().syncAllTrackedEntityAttributeReservedValues();
                    return SyncResult.success();
                })
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
    private Observable<Unit> metadata() {
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
            if (NetworkUtils.isOnline(view.getContext())) {
                syncMetadata();
                view.handleSync();
                sync();
            } else
                view.startActivity(MainActivity.class, null, true, true, null);
        }
    }

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Timber.e(throwable);
        if (throwable instanceof IOException) {
            view.renderInvalidServerUrlError();
        } else if (throwable instanceof D2CallException) {
            D2CallException d2CallException = (D2CallException) throwable;
            switch (d2CallException.errorCode()) {
                case LOGIN_PASSWORD_NULL:
                    view.renderError(d2CallException.errorCode());
                    break;
                case LOGIN_USERNAME_NULL:
                    view.renderError(d2CallException.errorCode());
                    break;
                case INVALID_DHIS_VERSION:
                    view.renderError(d2CallException.errorCode());
                    break;
                case ALREADY_AUTHENTICATED:
                    handleResponse(Response.success(null));
                    view.renderInvalidCredentialsError();
                    break;
                case API_UNSUCCESSFUL_RESPONSE:
                    view.renderError(d2CallException.errorCode());
                    break;
                case API_RESPONSE_PROCESS_ERROR:
                    view.renderError(d2CallException.errorCode());
                    break;
                default:
                    view.renderError(d2CallException.errorCode());
                    break;
            }
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
            disposable.add(Observable.fromCallable(
                    userManager.getD2().logout())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            data -> {
                                SharedPreferences prefs = view.getAbstracContext().getSharedPreferences("com.dhis2", Context.MODE_PRIVATE);
                                prefs.edit().putBoolean("SessionLocked", false).apply();
                                prefs.edit().putString("pin", null).apply();
                                view.handleLogout();
                            },
                            t -> view.handleLogout()
                    )
            );
    }

    private void syncMetadata() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences("com.dhis2", Context.MODE_PRIVATE);
        prefs.edit().putInt("timeMeta", TIME_DAILY).apply();
        syncMeta(TIME_DAILY);
    }

    private void syncMeta(int seconds) {
        String tag = "MetaData";
        Job metaJob;

        //boolean isRecurring = false;
        JobTrigger trigger = Trigger.NOW;

        /*if (seconds != 0) {
            isRecurring = true;
            trigger = Trigger.executionWindow(seconds, seconds + 60);
        }*/

        metaJob = dispatcher.newJobBuilder()
                .setService(SyncMetadataService.class)
                .setTag(tag)
                .setRecurring(false)
                .setTrigger(trigger)
                .setReplaceCurrent(true)
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(
                        Constraint.ON_ANY_NETWORK
                )
                .build();
        dispatcher.mustSchedule(metaJob);
    }
}