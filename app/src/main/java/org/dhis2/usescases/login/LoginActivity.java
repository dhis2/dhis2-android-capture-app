package org.dhis2.usescases.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import com.andrognito.pinlockview.PinLockListener;
import com.crashlytics.android.Crashlytics;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityLoginBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.sync.SyncActivity;
import org.dhis2.utils.BiometricStorage;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.OnDialogClickListener;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;

import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import de.adorsys.android.securestoragelibrary.SecurePreferences;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.Constants.RQ_QR_SCANNER;


public class LoginActivity extends ActivityGlobalAbstract implements LoginContracts.View {

    ActivityLoginBinding binding;

    @Inject
    LoginContracts.Presenter presenter;

    List<String> users;
    List<String> urls;

    private boolean isPinScreenVisible = false;
    private String qrUrl;


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
    public void showBiometricButton() {
        binding.biometricButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void checkSecuredCredentials() {
        if (SecurePreferences.contains(Constants.SECURE_SERVER_URL) &&
                SecurePreferences.contains(Constants.SECURE_USER_NAME) &&
                SecurePreferences.contains(Constants.SECURE_PASS)) {
            binding.serverUrlEdit.setText(SecurePreferences.getStringValue(Constants.SECURE_SERVER_URL, null));
            binding.userNameEdit.setText(SecurePreferences.getStringValue(Constants.SECURE_USER_NAME, null));
            binding.userPassEdit.setText(SecurePreferences.getStringValue(Constants.SECURE_PASS, null));
            showLoginProgress(true);
        } else
            showInfoDialog(getString(R.string.biometrics_dialog_title), getString(R.string.biometrics_first_use_text));
    }

    @Override
    public void goToNextScreen() {
        if (NetworkUtils.isOnline(this)) {
            startActivity(SyncActivity.class, null, true, true, null);
        } else
            startActivity(MainActivity.class, null, true, true, null);
    }

    @Override
    public void switchPasswordVisibility() {
        if (binding.userPassEdit.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            binding.visibilityButton.setImageDrawable(
                    ColorUtils.tintDrawableWithColor(
                            ContextCompat.getDrawable(this, R.drawable.ic_visibility),
                            ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY)));
            binding.userPassEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            binding.visibilityButton.setImageDrawable(
                    ColorUtils.tintDrawableWithColor(
                            ContextCompat.getDrawable(this, R.drawable.ic_visibility_off),
                            ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY)));
            binding.userPassEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        binding.userPassEdit.setSelection(binding.userPassEdit.getText().length());
    }

    @Override
    public void setUrl(String url) {
        binding.serverUrlEdit.setText(!isEmpty(qrUrl) ? qrUrl : url);
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
                message = getString(R.string.login_error_dhis_version_v2);
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

            presenter.logIn(
                    binding.serverUrl.getEditText().getText().toString(),
                    binding.userName.getEditText().getText().toString(),
                    binding.userPass.getEditText().getText().toString()
            );
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            binding.credentialLayout.setVisibility(View.VISIBLE);
            binding.progressLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showCrashlyticsDialog() {
        showInfoDialog(getString(R.string.send_user_name_title), getString(R.string.send_user_name_mesage),
                new OnDialogClickListener() {
                    @Override
                    public void onPossitiveClick(AlertDialog alertDialog) {
                        getSharedPreferences().edit().putBoolean(Constants.USER_ASKED_CRASHLYTICS, true).apply();
                        getSharedPreferences().edit().putString(Constants.USER, binding.userName.getEditText().getText().toString()).apply();
                        showLoginProgress(true);
                    }

                    @Override
                    public void onNegativeClick(AlertDialog alertDialog) {
                        getSharedPreferences().edit().putBoolean(Constants.USER_ASKED_CRASHLYTICS, true).apply();
                        showLoginProgress(true);
                    }
                }).show();
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

        if (false && presenter.canHandleBiometrics() && //TODO: Remove false when green light
                (!BiometricStorage.areCredentialsSet() &&
                        !BiometricStorage.areSameCredentials(
                                binding.serverUrlEdit.getText().toString(),
                                binding.userNameEdit.getText().toString(),
                                binding.userPassEdit.getText().toString()))) {
            showInfoDialog(getString(R.string.biometrics_security_title),
                    getString(R.string.biometrics_security_text),
                    new OnDialogClickListener() {
                        @Override
                        public void onPossitiveClick(AlertDialog alertDialog) {
                            BiometricStorage.saveUserCredentials(
                                    binding.serverUrlEdit.getText().toString(),
                                    binding.userNameEdit.getText().toString(),
                                    binding.userPassEdit.getText().toString());
                            goToNextScreen();
                        }

                        @Override
                        public void onNegativeClick(AlertDialog alertDialog) {
                            goToNextScreen();
                        }
                    }).show();
        } else
            goToNextScreen();

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
            qrUrl = data.getStringExtra(Constants.EXTRA_DATA);
        }
    }


}