package org.dhis2.usescases.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.github.pwittchen.rxbiometric.library.RxBiometric;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.server.ConfigurationRepository;
import org.dhis2.data.server.UserManager;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.qrScanner.QRActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableField;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

import static org.dhis2.utils.Constants.PIN;
import static org.dhis2.utils.Constants.SESSION_LOCKED;

public class LoginPresenterImpl implements LoginContracts.LoginPresenter {

    private final ConfigurationRepository configurationRepository;
    private LoginContracts.LoginView view;

    private UserManager userManager;
    private CompositeDisposable disposable;

    private ObservableField<Boolean> isServerUrlSet = new ObservableField<>(false);
    private ObservableField<Boolean> isUserNameSet = new ObservableField<>(false);
    private ObservableField<Boolean> isUserPassSet = new ObservableField<>(false);
    private Boolean canHandleBiometrics;

    LoginPresenterImpl(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    @SuppressWarnings("squid:CommentedOutCodeLine")
    public void init(LoginContracts.LoginView view) {
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
                        if (isUserLoggedIn && !prefs.getBoolean(SESSION_LOCKED, false)) {
                            view.startActivity(MainActivity.class, null, true, true, null);
                        } else if (prefs.getBoolean(SESSION_LOCKED, false)) {
                            view.getBinding().unlockLayout.setVisibility(View.VISIBLE);
                        }

                    }, Timber::e));

            disposable.add(
                    Observable.just(userManager.getD2().systemInfoModule().systemInfo.getWithAllChildren() != null ?
                            userManager.getD2().systemInfoModule().systemInfo.getWithAllChildren() : SystemInfo.builder().build())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    systemInfo -> {
                                        if (systemInfo.contextPath() != null)
                                            view.setUrl(systemInfo.contextPath());
                                        else
                                            view.setUrl(view.getContext().getString(R.string.login_https));
                                    },
                                    Timber::e));
        } else
            view.setUrl(view.getContext().getString(R.string.login_https));


        //TODO: UNCOMMENT WHEN GREEN LIGHT
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            disposable.add(RxPreconditions
//                    .hasBiometricSupport(view.getContext())
//                    .filter(canHandleBiometrics -> {
//                        this.canHandleBiometrics = canHandleBiometrics;
//                        return canHandleBiometrics && SecurePreferences.contains(Constants.SECURE_SERVER_URL);
//                    })
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            canHandleBiometrics -> view.showBiometricButton(),
//                            Timber::e));
    }

    @Override
    public void onServerChanged(CharSequence s, int start, int before, int count) {
        isServerUrlSet.set(!view.getBinding().serverUrl.getEditText().getText().toString().isEmpty());
        view.resetCredentials(false, true, true);

        if (isServerUrlSet.get() &&
                (view.getBinding().serverUrl.getEditText().getText().toString().equals(Constants.URL_TEST_229) ||
                        view.getBinding().serverUrl.getEditText().getText().toString().equals(Constants.URL_TEST_230))) {
            view.setTestingCredentials();
        }

        view.setLoginVisibility(isServerUrlSet.get() && isUserNameSet.get() && isUserPassSet.get());


    }

    @Override
    public void onUserChanged(CharSequence s, int start, int before, int count) {
        isUserNameSet.set(!view.getBinding().userName.getEditText().getText().toString().isEmpty());
        view.resetCredentials(false, false, true);

        view.setLoginVisibility(isServerUrlSet.get() && isUserNameSet.get() && isUserPassSet.get());

    }

    @Override
    public void onPassChanged(CharSequence s, int start, int before, int count) {
        isUserPassSet.set(!view.getBinding().userPass.getEditText().getText().toString().isEmpty());
        view.setLoginVisibility(isServerUrlSet.get() && isUserNameSet.get() && isUserPassSet.get());
    }

    @Override
    public void onButtonClick() {
        view.hideKeyboard();
        view.showLoginProgress(true);
    }

    @Override
    public void logIn(String serverUrl, String userName, String pass) {
        HttpUrl baseUrl = HttpUrl.parse(canonizeUrl(serverUrl));
        if (baseUrl == null) {
            return;
        }
        disposable.add(
                configurationRepository.configure(baseUrl)
                        .map(config -> ((App) view.getAbstractActivity().getApplicationContext()).createServerComponent(config).userManager())
                        .switchMap(userManagerResult -> {
                            SharedPreferences prefs = view.getAbstractActivity().getSharedPreferences(
                                    Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                            prefs.edit().putString(Constants.SERVER, serverUrl).apply();
                            userManager = userManagerResult;
                            return userManager.logIn(userName.trim(), pass).map(user -> {
                                if (user == null)
                                    return Response.error(404, ResponseBody.create(MediaType.parse("text"), "NOT FOUND"));
                                else {
                                    return Response.success(null);
                                }
                            });
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::handleResponse,
                                this::handleError));
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
    public void onVisibilityClick(View v) {
        view.switchPasswordVisibility();
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
        if (prefs.getString(PIN, "").equals(pin)) {
            prefs.edit().putBoolean(SESSION_LOCKED, false).apply();
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
                    userManager.getD2().userModule().logOut())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            data -> {
                                SharedPreferences prefs = view.getAbstracContext().getSharedPreferences();
                                prefs.edit().putBoolean(SESSION_LOCKED, false).apply();
                                prefs.edit().putString(PIN, null).apply();
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
        view.showLoginProgress(false);
        if (userResponse.isSuccessful()) {
            ((App) view.getContext().getApplicationContext()).createUserComponent();
            view.saveUsersData();
        }

    }

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Timber.e(throwable);
        if (throwable instanceof IOException) {
            view.renderInvalidServerUrlError();
        } else if (throwable instanceof D2Error) {
            D2Error d2CallException = (D2Error) throwable;
            org.hisp.dhis.android.core.maintenance.D2ErrorCode i = d2CallException.errorCode();
            if (i == org.hisp.dhis.android.core.maintenance.D2ErrorCode.ALREADY_AUTHENTICATED) {
                handleResponse(Response.success(null));

            } else {
                view.renderError(d2CallException.errorCode(), d2CallException.errorDescription());

            }
        } else {
            view.renderUnexpectedError();
        }

        view.showLoginProgress(false);
    }

    @Override
    public void onFingerprintClick() {
        disposable.add(
                RxBiometric
                        .title("Title")
                        .description("description")
                        .negativeButtonText("Cancel")
                        .negativeButtonListener((dialog, which) -> {
                        })
                        .executor(ActivityCompat.getMainExecutor(view.getAbstractActivity()))
                        .build()
                        .authenticate(view.getAbstractActivity())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> view.checkSecuredCredentials(),
                                error -> view.displayMessage("AUTH ERROR")));
    }

    @Override
    public Boolean canHandleBiometrics() {
        return canHandleBiometrics;
    }


}