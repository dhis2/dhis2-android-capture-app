package org.dhis2.usescases.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.view.View;

import org.dhis2.App;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.server.ConfigurationRepository;
import org.dhis2.data.server.UserManager;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.qrScanner.QRActivity;
import org.dhis2.usescases.sync.SyncActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.maintenance.D2Error;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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

    LoginPresenter(ConfigurationRepository configurationRepository, MetadataRepository metadataRepository) {
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
        //view.handleSync();

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
                    return userManager.logIn(username.trim(), password);
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

    @Override
    public void onTestingEnvironmentClick(int dhisVersion) {
        switch (dhisVersion) {
            case 29:
                view.getBinding().serverUrl.getEditText().setText("https://play.dhis2.org/android-previous1");
                break;
            case 30:
                view.getBinding().serverUrl.getEditText().setText("https://play.dhis2.org/android-current");
                break;
        }

        view.getBinding().userName.getEditText().setText("android");
        view.getBinding().userPass.getEditText().setText("Android123");

        onButtonClick();
    }

    private String canonizeUrl(@NonNull String serverUrl) {
        String urlToCanonized = serverUrl.trim();
        urlToCanonized = urlToCanonized.replace(" ", "");
        return urlToCanonized.endsWith("/") ? urlToCanonized : urlToCanonized + "/";
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
                metadataRepository.createErrorTable();
                view.startActivity(SyncActivity.class, null, true, true, null);
            } else
                view.startActivity(MainActivity.class, null, true, true, null);
        }
    }

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Timber.e(throwable);
        if (throwable instanceof IOException) {
            view.renderInvalidServerUrlError();
        } else if (throwable instanceof D2Error) {
            D2Error d2CallException = (D2Error) throwable;
            switch (d2CallException.errorCode()) {
                case ALREADY_AUTHENTICATED:
                    handleResponse(Response.success(null));
                    break;
                default:
                    view.renderError(d2CallException.errorCode(), d2CallException.errorDescription());
                    break;
            }
        } else {
            view.renderUnexpectedError();
        }
    }


}