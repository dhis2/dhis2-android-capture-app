package org.dhis2.usescases.login;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
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
import org.dhis2.databinding.ActivityLoginBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.CustomViews.CustomDialog;
import org.dhis2.utils.DialogClickListener;

import org.hisp.dhis.android.core.common.D2ErrorCode;

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
    private boolean isSyncing;

    private boolean isPinScreenVisible = false;

    private CustomDialog customDialog;

    enum SyncState {
        METADATA, EVENTS, TEI
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

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
        if (!isSyncing)
            presenter.init(this);
    }

    @Override
    protected void onPause() {
        if (!isSyncing)
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
    public void renderError(D2ErrorCode errorCode) {
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
                message = getString(R.string.login_error_default);
                break;
        }

        customDialog = new CustomDialog(this,
                getString(R.string.login_error),
                message,getString(R.string.button_ok), getString(R.string.cancel), errorCode.hashCode(), new DialogClickListener() {
            @Override
            public void onPositive() {
                customDialog.dismiss();
            }

            @Override
            public void onNegative() {
                customDialog.dismiss();
            }
        });
        customDialog.show();

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
    public void handleSync() {
        isSyncing = true;
        binding.login.setVisibility(View.GONE);
        if (binding.logo != null) {
            ViewGroup.LayoutParams params = binding.logo.getLayoutParams();
            params.height = MATCH_PARENT;
            binding.logo.setLayoutParams(params);
            binding.syncLayout.setVisibility(View.VISIBLE);
            binding.lottieView.setVisibility(View.VISIBLE);
            binding.lottieView.setRepeatMode(LottieDrawable.INFINITE);
            binding.lottieView.useHardwareAcceleration(true);
            binding.lottieView.enableMergePathsForKitKatAndAbove(true);
            binding.lottieView.playAnimation();
        }
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

    @NonNull
    @Override
    public Consumer<SyncResult> update(SyncState syncState) {
        return result -> {
            if (result.inProgress()) {
                if (syncState == SyncState.METADATA)
                    binding.metadataText.setText(getString(R.string.syncing_configuration));
                else {
                    binding.eventsText.setText(getString(R.string.syncing_data));
                    Bindings.setDrawableEnd(binding.eventsText, ContextCompat.getDrawable(this, R.drawable.animator_sync));
                    binding.eventsText.setAlpha(1.0f);
                }
            } else if (result.isSuccess()) {
                if (syncState == SyncState.METADATA) {
                    binding.metadataText.setText(getString(R.string.configuration_ready));
                    Bindings.setDrawableEnd(binding.metadataText, ContextCompat.getDrawable(this, R.drawable.animator_done));
                } else if (syncState == SyncState.TEI) {
                    binding.eventsText.setText(getString(R.string.data_ready));
                    Bindings.setDrawableEnd(binding.eventsText, ContextCompat.getDrawable(this, R.drawable.animator_done));
                }
                presenter.syncNext(syncState, result);
            } else if (!result.isSuccess()) {
                if (syncState == SyncState.METADATA) {
                    binding.metadataText.setText(getString(R.string.configuration_sync_failed));
                    binding.metadataText.setCompoundDrawables(null, null, ContextCompat.getDrawable(this, R.drawable.ic_sync_error_black), null);
                } else if (syncState == SyncState.TEI) {
                    binding.eventsText.setText(getString(R.string.data_sync_failed));
                    binding.eventsText.setCompoundDrawables(null, null, ContextCompat.getDrawable(this, R.drawable.ic_sync_error_black), null);
                }

                presenter.syncNext(syncState, result);

            } else {
                throw new IllegalStateException();
            }
        };
    }

    @Override
    public void saveTheme(Integer themeId) {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                "org.dhis2", Context.MODE_PRIVATE);
        prefs.edit().putInt("THEME", themeId).apply();
        setTheme(themeId);

        int startColor = ContextCompat.getColor(this, R.color.colorPrimary);
        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int endColor = a.getColor(0, 0);
        a.recycle();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(2000); // milliseconds
        colorAnimation.addUpdateListener(animator -> binding.logo.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();

    }

    @Override
    public void saveFlag(String s) {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                "org.dhis2", Context.MODE_PRIVATE);
        prefs.edit().putString("FLAG", s).apply();

        binding.logoFlag.setImageResource(getResources().getIdentifier(s, "drawable", getPackageName()));
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0f, 1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.addUpdateListener(animation -> {
            binding.logoFlag.setAlpha((float) animation.getAnimatedValue());
        });
        alphaAnimator.start();

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