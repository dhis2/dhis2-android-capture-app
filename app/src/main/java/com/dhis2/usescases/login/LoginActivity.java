package com.dhis2.usescases.login;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.andrognito.pinlockview.PinLockListener;
import com.dhis2.App;
import com.dhis2.Bindings.Bindings;
import com.dhis2.R;
import com.dhis2.databinding.ActivityLoginBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.Constants;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.dhis2.utils.Constants.RQ_QR_SCANNER;


public class LoginActivity extends ActivityGlobalAbstract implements LoginContracts.View {

    ActivityLoginBinding binding;

    @Inject
    LoginContracts.Presenter presenter;

    List<String> users;
    List<String> urls;
    private boolean isSyncing;


    enum SyncState {
        METADATA, EVENTS, TEI
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
    public ActivityLoginBinding getBinding() {
        return binding;
    }

    @Override
    public void showProgress() {
        binding.progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        binding.progress.setVisibility(View.GONE);
    }

    @Override
    public void renderInvalidServerUrlError() {
        binding.serverUrl.setError(getResources().getString(R.string.error_wrong_server_url));
    }

    @Override
    public void renderInvalidCredentialsError() {
        binding.userName.setError(getString(R.string.error_wrong_credentials));
        binding.userPass.setError(getString(R.string.error_wrong_credentials));
    }

    @Override
    public void renderUnexpectedError() {
        Toast.makeText(this, getResources().getString(
                R.string.error_unexpected_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void renderServerError() {
        Toast.makeText(this, getResources().getString(
                R.string.error_internal_server_error), Toast.LENGTH_SHORT).show();
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
                presenter.syncNext(syncState);
            } else if (!result.isSuccess()) {
                if (syncState == SyncState.METADATA) {
                    binding.metadataText.setText(getString(R.string.configuration_sync_failed));
                    binding.metadataText.setCompoundDrawables(null, null, ContextCompat.getDrawable(this, R.drawable.ic_sync_error_black), null);
                } else if (syncState == SyncState.TEI) {
                    binding.eventsText.setText(getString(R.string.data_sync_failed));
                    binding.eventsText.setCompoundDrawables(null, null, ContextCompat.getDrawable(this, R.drawable.ic_sync_error_black), null);
                }

                presenter.syncNext(syncState);

            } else {
                throw new IllegalStateException();
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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