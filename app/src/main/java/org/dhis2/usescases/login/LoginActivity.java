package org.dhis2.usescases.login;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.airbnb.lottie.LottieDrawable;
import com.andrognito.pinlockview.PinLockListener;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.service.SyncResult;
import org.dhis2.databinding.ActivityLoginBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.dhis2.utils.Constants.RQ_QR_SCANNER;


public class LoginActivity extends ActivityGlobalAbstract implements LoginContracts.View {

    ActivityLoginBinding binding;

    @Inject
    LoginContracts.Presenter presenter;

    List<String> users;
    List<String> urls;

    private boolean isPinScreenVisible = false;

    enum SyncState {
        METADATA, EVENTS, TEI, RESERVED_VALUES, AGGREGATES
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LoginComponent loginComponent = ((App) getApplicationContext()).loginComponent();
        if (loginComponent == null) {
            // in case if we don't have cached presenter
            loginComponent = ((App) getApplicationContext()).createLoginComponent();
        }
        loginComponent.inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setPresenter(presenter);

        binding.testingEnvironment.login229.setOnClickListener(
                view -> {
                    presenter.onTestingEnvironmentClick(29);
                }
        );

        binding.testingEnvironment.login230.setOnClickListener(
                view -> {
                    presenter.onTestingEnvironmentClick(30);
                }
        );

        setAutocompleteAdapters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);

        NetworkUtils.isGooglePlayServicesAvailable(this);
    }

    @Override
    protected void onPause() {
        presenter.onDestroy();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ((App) getApplicationContext()).releaseLoginComponent();
        super.onDestroy();
    }

    @Override
    public ActivityLoginBinding getBinding() {
        return binding;
    }

    @Override
    public void renderError(D2ErrorCode errorCode, String defaultMessage) {
        String message;
        switch (errorCode) {
            case LOGIN_PASSWORD_NULL:
                message = getString(R.string.login_error_null_pass);
                break;
            case LOGIN_USERNAME_NULL:
                message = getString(R.string.login_error_null_username);
                break;
            case INVALID_DHIS_VERSION:
                message = getString(R.string.login_error_dhis_version);
                break;
            case API_UNSUCCESSFUL_RESPONSE:
                message = getString(R.string.login_error_unsuccessful_response);
                break;
            case API_RESPONSE_PROCESS_ERROR:
                message = getString(R.string.login_error_error_response);
                break;
            default:
                message = String.format("%s\n%s", getString(R.string.login_error_default), defaultMessage);
                break;
        }

        showInfoDialog(getString(R.string.login_error), message);

    }

    @Override
    public void renderInvalidServerUrlError() {
        binding.serverUrl.setError(getResources().getString(R.string.error_wrong_server_url));
    }

    @Override
    public void renderInvalidCredentialsError() {
        displayMessage(getResources().getString(R.string.error_wrong_credentials));
    }

    @Override
    public void renderUnexpectedError() {
        displayMessage(getResources().getString(R.string.error_unexpected_error));
    }

    @Override
    public void renderEmptyUsername() {
        binding.userName.setError(getString(R.string.error_wrong_credentials));
    }

    @Override
    public void renderEmptyPassword() {
        binding.userPass.setError(getString(R.string.error_wrong_credentials));
    }

    @Override
    public void renderServerError() {
        displayMessage(getResources().getString(R.string.error_internal_server_error));
    }



    @Override
    public void handleLogout() {
        recreate();
    }

    @Override
    public void setLoginVisibility(boolean isVisible) {
        binding.login.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUnlockClick(View android) {
        binding.pinLayout.pinLockView.attachIndicatorDots(binding.pinLayout.indicatorDots);
        binding.pinLayout.pinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                presenter.unlockSession(pin);
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        });
        binding.pinLayout.getRoot().setVisibility(View.VISIBLE);
        isPinScreenVisible = true;
    }

    @Override
    public void onLogoutClick(View android) {
        presenter.logOut();
    }

    @Override
    public void setAutocompleteAdapters() {

        urls = getListFromPreference(Constants.PREFS_URLS);
        users = getListFromPreference(Constants.PREFS_USERS);

        ArrayAdapter<String> urlAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urls);
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, users);

        binding.serverUrlEdit.setAdapter(urlAdapter);
        binding.userNameEdit.setAdapter(userAdapter);
    }

    @Override
    public void saveUsersData() {
        if (urls != null && !urls.contains(binding.serverUrlEdit.getText().toString())) {
            urls.add(binding.serverUrlEdit.getText().toString());
            saveListToPreference(Constants.PREFS_URLS, urls);
        }
        if (users != null && !users.contains(binding.userNameEdit.getText().toString())) {
            users.add(binding.userNameEdit.getText().toString());
            saveListToPreference(Constants.PREFS_USERS, users);
        }
    }


    @Override
    public void onBackPressed() {
        if (isPinScreenVisible) {
            binding.pinLayout.getRoot().setVisibility(View.GONE);
            isPinScreenVisible = false;
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_QR_SCANNER && resultCode == RESULT_OK) {
            binding.serverUrlEdit.setText(data.getStringExtra(Constants.EXTRA_DATA));
        }
    }


}