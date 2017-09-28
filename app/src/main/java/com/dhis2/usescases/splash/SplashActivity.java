package com.dhis2.usescases.splash;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;

import com.dhis2.R;
import com.dhis2.databinding.ActivitySplashBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.login.LoginActivity;

public class SplashActivity extends ActivityGlobalAbstract implements SplashContracts.View {

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        binding.setPresenter(new SplashPresenter(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SplashActivity.this, binding.logo, ViewCompat.getTransitionName(binding.logo));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(LoginActivity.class,null,true,true,optionsCompat);
            }
        }, 2000);


    }
}