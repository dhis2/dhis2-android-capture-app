package com.dhis2.usescases.splash;

import com.dhis2.usescases.login.LoginActivity;
import com.dhis2.usescases.main.MainActivity;

public class SplashRouter implements SplashContractsModule.Router {

    private SplashContractsModule.View view;

    SplashRouter(SplashContractsModule.View view) {
        this.view = view;
    }

    @Override
    public void navigateToLoginView() {
        view.startActivity(LoginActivity.class, null, true, true, null);
    }

    @Override
    public void navigateToHomeView() {
        view.startActivity(MainActivity.class, null, true, true, null);
    }
}