package com.dhis2.usescases.splash;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.dhis2.App;
import com.dhis2.AppComponent;
import com.dhis2.R;
import com.dhis2.data.server.ServerComponent;
import com.dhis2.databinding.ActivitySplashBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

public class SplashActivity extends ActivityGlobalAbstract implements SplashContracts.View {

    ActivitySplashBinding binding;

    @Inject
    SplashContracts.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        AppComponent appComponent = ((App) getApplicationContext()).appComponent();
        ServerComponent serverComponent = ((App) getApplicationContext()).serverComponent();
        appComponent.plus(new SplashModule(serverComponent)).inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
        presenter.isUserLoggedIn();
    }

    @Override
    protected void onPause() {
        presenter.destroy();
        super.onPause();
    }
}