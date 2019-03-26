package org.dhis2.usescases.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;

import com.github.pwittchen.rxbiometric.library.RxBiometric;
import com.github.pwittchen.rxbiometric.library.validation.RxPreconditions;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.server.ConfigurationRepository;
import org.dhis2.data.server.UserManager;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.qrScanner.QRActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import de.adorsys.android.securestoragelibrary.SecurePreferences;
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
    private LoginContracts.View view;

    private UserManager userManager;
    private CompositeDisposable disposable;

    private Boolean canHandleBiometrics;

    LoginPresenter(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
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
                            view.showUnlockButton();
                        }

                    }, Timber::e));

            disposable.add(
                    Observable.just(userManager.getD2().systemInfoModule().systemInfo.get() != null ?
                            userManager.getD2().systemInfoModule().systemInfo.get() : SystemInfo.builder().build())
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


        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //TODO: REMOVE FALSE WHEN GREEN LIGHT
            disposable.add(RxPreconditions
                    .hasBiometricSupport(view.getContext())
                    .filter(canHandleBiometrics -> {
                        this.canHandleBiometrics = canHandleBiometrics;
                        return canHandleBiometrics && SecurePreferences.contains(Constants.SECURE_SERVER_URL);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            canHandleBiometrics -> view.showBiometricButton(),
                            Timber::e));


    }

    @Override
    public void onButtonClick() {
        view.hideKeyboard();
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(Constants.USER_ASKED_CRASHLYTICS, false))
            view.showCrashlyticsDialog();
        else
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
                        .switchMap(userManager -> {
                            SharedPreferences prefs = view.getAbstractActivity().getSharedPreferences(
                                    Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                            prefs.edit().putString(Constants.SERVER, serverUrl).apply();
                            this.userManager = userManager;
                            return userManager.logIn(userName.trim(), pass).map(user -> {
                                if (user == null)
                                    return Response.error(404, ResponseBody.create(MediaType.parse("text"), "NOT FOUND"));
                                else {
                                    prefs.edit().putString(Constants.USER, user.userCredentials().username());
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
                    userManager.getD2().userModule().logOut())
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
        view.showLoginProgress(false);
        if (userResponse.isSuccessful()) {
            ((App) view.getContext().getApplicationContext()).createUserComponent();
            view.saveUsersData();
        }
    }

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Timber.e(throwable);
        if (throwable instanceof D2Error && ((D2Error) throwable).errorCode() == D2ErrorCode.ALREADY_AUTHENTICATED)
            handleResponse(Response.success(null));
        else
            view.renderError(throwable);
        view.showLoginProgress(false);
    }

    //region FINGERPRINT
    @Override
    public Boolean canHandleBiometrics() {
        return canHandleBiometrics;
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


    //endregion


}