package com.dhis2.usescases.splash;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.dhis2.R;
import com.dhis2.databinding.ActivitySplashBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SplashActivity extends ActivityGlobalAbstract implements SplashContractsModule.View {

    ActivitySplashBinding binding;
    @Inject
    SplashPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        binding.setPresenter(presenter);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }
}