package org.dhis2.usescases.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;

import org.dhis2.App;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.server.ConfigurationRepository;
import org.dhis2.data.server.UserManager;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.qrScanner.QRActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;

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
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class LoginPresenter implements LoginContracts.Presenter {

    private final ConfigurationRepository configurationRepository;
    private final MetadataRepository metadataRepository;
    private LoginContracts.View view;

    private UserManager userManager;
    private CompositeDisposable disposable;

    public ObservableField<Boolean> isServerUrlSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserNameSet = new ObservableField<>(false);
    public ObservableField<Boolean> isUserPassSet = new ObservableField<>(false);

    LoginPresenter(ConfigurationRepository configurationRepository, MetadataRepository metadataRepository, FirebaseJobDispatcher jobDispatcher) {
        this.configurationRepository = configurationRepository;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(LoginContracts.View view) {
        this.view = view;

        this.disposable = new CompositeDisposable();
        userManager = null;
        if (((App) view.getContext().getApplicationContext()).getServerComponent() != null)
            userManager = ((App) view.getContext().getApplicationContext()).getServerComponent().userManager();

        if (userManager != null) {
            disposable.add(userManager.isUserLoggedIn()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isUserLoggedIn -> {
                        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                        if (isUserLoggedIn && !prefs.getBoolean("SessionLocked", false)) {
                            view.startActivity(MainActivity.class, null, true, true, null);
                        } else if (prefs.getBoolean("SessionLocked", false)) {
                            view.getBinding().unlockLayout.setVisibility(View.VISIBLE);
                        }

                    }, Timber::e));
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isServerUrlSet.set(!view.getBinding().serverUrl.getEditText().getText().toString().isEmpty());
        isUserNameSet.set(!view.getBinding().userName.getEditText().getText().toString().isEmpty());
        isUserPassSet.set(!view.getBinding().userPass.getEditText().getText().toString().isEmpty());

        view.setLoginVisibility(isServerUrlSet.get() && isUserNameSet.get() && isUserPassSet.get());
    }

    @Override
    public void onButtonClick() {
        view.hideKeyboard();

        String serverUrl = view.getBinding().serverUrl.getEditText().getText().toString();
        String username = view.getBinding().userName.getEditText().getText().toString();
        String password = view.getBinding().userPass.getEditText().getText().toString();

        HttpUrl baseUrl = HttpUrl.parse(canonizeUrl(serverUrl));
        if (baseUrl == null) {
            return;
        }

        disposable.add(configurationRepository.configure(baseUrl)
                .map(config -> ((App) view.getAbstractActivity().getApplicationContext()).createServerComponent(config).userManager())
                .switchMap(userManager -> {
                    SharedPreferences prefs = view.getAbstractActivity().getSharedPreferences(
                            Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                    prefs.edit().putString(Constants.SERVER, serverUrl).apply();
                    this.userManager = userManager;
                    return userManager.logIn(username, password);
                })
                .map(user -> {
                    if (user == null)
                        return Response.error(404, ResponseBody.create(MediaType.parse("text"), "NOT FOUND"));
                    else {
                        return Response.success(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleResponse,
                        this::handleError));
    }

    private String canonizeUrl(@NonNull String serverUrl) {
        return serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";
    }

    @Override
    public void onQRClick(View v) {
        Intent intent = new Intent(view.getContext(), QRActivity.class);
        view.getAbstractActivity().startActivityForResult(intent, Constants.RQ_QR_SCANNER);
    }

    @Override
    public ObservableField<Boolean> isServerUrlSet() {
        return isServerUrlSet;
    }

    @Override
    public ObservableField<Boolean> isUserNameSet() {
        return isUserNameSet;
    }

    @Override
    public ObservableField<Boolean> isUserPassSet() {
        return isServerUrlSet;
    }

    @Override
    public void unlockSession(String pin) {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (prefs.getString("pin", "").equals(pin)) {
            prefs.edit().putBoolean("SessionLocked", false).apply();
            view.startActivity(MainActivity.class, null, true, true, null);
        }
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }

    @Override
    public void syncNext(LoginActivity.SyncState syncState, SyncResult syncResult) {
        if (syncResult.isSuccess() || syncState != LoginActivity.SyncState.METADATA)
            switch (syncState) {
                case METADATA:
                    syncEvents();
                    break;
                case EVENTS:
                    syncReservedValues();
                    syncTrackedEntities();
                    break;
                case TEI:
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    view.getContext().startActivity(intent);
                    view.getAbstractActivity().finish();
                    break;
                default:
                    break;
            }
        else {
            view.displayMessage("Something went wrong during syncronisation");
            new Handler().postDelayed(this::logOut, 1500);
        }
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
                                SharedPreferences prefs = view.getAbstracContext().getSharedPreferences();
                                prefs.edit().putBoolean("SessionLocked", false).apply();
                                prefs.edit().putString("pin", null).apply();
                                view.handleLogout();
                            },
                            t -> view.handleLogout()
                    )
            );
    }

    @Override
    public void handleResponse(@NonNull Response userResponse) {
        Timber.d("Authentication response url: %s", userResponse.raw().request().url().toString());
        Timber.d("Authentication response code: %s", userResponse.code());
        if (userResponse.isSuccessful()) {
            ((App) view.getContext().getApplicationContext()).createUserComponent();
            view.saveUsersData();
            if (NetworkUtils.isOnline(view.getContext())) {
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

    @Override
    public void sync() {
        disposable.add(metadata()
                .map(response -> SyncResult.success())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(syncReult -> view.update(LoginActivity.SyncState.METADATA).accept(syncReult),
                        Timber::d)
        );
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
                    /*for (Pair<String, String> pair : pairs) {
                        userManager.getD2().popTrackedEntityAttributeReservedValue(pair.val0(), pair.val1());
                    }*/
                    userManager.getD2().syncAllTrackedEntityAttributeReservedValues();
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
    private Consumer<SyncResult> update(LoginActivity.SyncState syncState) {
        return result -> {
            if (view != null) {
                view.update(syncState).accept(result);
            }
        };
    }

    @NonNull
    private Observable<Unit> metadata() {
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().syncMetaData()));
    }

    @NonNull
    private Observable<List<TrackedEntityInstance>> trackerData() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        int teiLimit = prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT);
        boolean limityByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);
        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().downloadTrackedEntityInstances(teiLimit, limityByOU)));
    }

    @NonNull
    private Observable<List<Event>> events() {
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        int eventLimit = prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT);
        boolean limityByOU = prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false);

        return Observable.defer(() -> Observable.fromCallable(userManager.getD2().downloadSingleEvents(eventLimit, limityByOU)));
    }

}