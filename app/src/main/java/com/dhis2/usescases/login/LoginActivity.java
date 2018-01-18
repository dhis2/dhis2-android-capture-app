package com.dhis2.usescases.login;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.andrognito.pinlockview.PinLockListener;
import com.dhis2.R;
import com.dhis2.databinding.ActivityLoginBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.Constants;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.dhis2.utils.Constants.RQ_QR_SCANNER;


public class LoginActivity extends ActivityGlobalAbstract implements LoginContractsModule.View {

    ActivityLoginBinding binding;

    @Inject
    LoginPresenter presenter;

    List<String> users;
    List<String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setPresenter(presenter);
        setAutocompleteAdapters();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        binding.login.setVisibility(View.GONE);
        ViewGroup.LayoutParams params = binding.logo.getLayoutParams();
        params.height = MATCH_PARENT;
        binding.logo.setLayoutParams(params);
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

        ArrayAdapter<String> urlAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, urls);
        ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, users);

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