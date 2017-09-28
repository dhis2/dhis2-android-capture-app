package com.dhis2.usescases.login;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.dhis2.R;
import com.dhis2.databinding.ActivityLoginBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;


public class LoginActivity extends ActivityGlobalAbstract implements LoginContracts.View {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setPresenter(new LoginPresenter(this));

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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}