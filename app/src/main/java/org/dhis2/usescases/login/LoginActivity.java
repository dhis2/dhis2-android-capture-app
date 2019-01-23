package org.dhis2.usescases.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import com.andrognito.pinlockview.PinLockListener;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityLoginBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;

import java.util.List;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;

import static org.dhis2.utils.Constants.RQ_QR_SCANNER;


public class LoginActivity extends ActivityGlobalAbstract implements LoginContracts.View {

    ActivityLoginBinding binding;

    @Inject
    LoginContracts.Presenter presenter;

    List<String> users;
    List<String> urls;

    private boolean isPinScreenVisible = false;


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
                view -> presenter.onTestingEnvironmentClick(29)
        );

        binding.testingEnvironment.login230.setOnClickListener(
                view -> presenter.onTestingEnvironmentClick(30)
        );

        setAutocompleteAdapters();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            checkFingerprintSensor();*/
    }

    /*@TargetApi(28)
    private void checkFingerprintSensor() {
        final BiometricPrompt promptInfo = new BiometricPrompt.Builder(this)
                .setTitle("Title goes here")
                .setSubtitle("Subtitle goes here")
                .setDescription("Description is good")
                .setNegativeButton("Cancel", Executors.newSingleThreadExecutor(), (dialog, which) -> dialog.dismiss())
                .build();

        promptInfo.authenticate(new CancellationSignal(), Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

    }*/

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
    public void renderUnexpectedError() {
        displayMessage(getResources().getString(R.string.error_unexpected_error));
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
    public void setTestingCredentials() {
        binding.userNameEdit.setText(Constants.USER_TEST_ANDROID);
        binding.userPassEdit.setText(Constants.USER_TEST_ANDROID_PASS);
    }

    @Override
    public void resetCredentials(boolean resetServer, boolean resetUser, boolean resetPass) {
        if (resetServer)
            binding.serverUrlEdit.setText(null);
        if (resetUser)
            binding.userNameEdit.setText(null);
        if (resetPass)
            binding.userPassEdit.setText(null);
    }

    @Override
    public void showLoginProgress(boolean showLogin) {
        if (showLogin) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            binding.credentialLayout.setVisibility(View.GONE);
            binding.progressLayout.setVisibility(View.VISIBLE);

        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            binding.credentialLayout.setVisibility(View.VISIBLE);
            binding.progressLayout.setVisibility(View.GONE);

        }
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

        if (!urls.contains(Constants.URL_TEST_229))
            urls.add(Constants.URL_TEST_229);
        if (!urls.contains(Constants.URL_TEST_230))
            urls.add(Constants.URL_TEST_230);
        if (!users.contains(Constants.USER_TEST_ANDROID))
            users.add(Constants.USER_TEST_ANDROID);

        saveListToPreference(Constants.PREFS_URLS, urls);
        saveListToPreference(Constants.PREFS_USERS, users);

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