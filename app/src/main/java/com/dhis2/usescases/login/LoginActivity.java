package com.dhis2.usescases.login;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Toast;

import com.dhis2.R;
import com.dhis2.databinding.ActivityLoginBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

import dagger.android.AndroidInjection;


public class LoginActivity extends ActivityGlobalAbstract implements LoginContractsModule.View {

    ActivityLoginBinding binding;

    @Inject
    LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setPresenter(presenter);
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
}