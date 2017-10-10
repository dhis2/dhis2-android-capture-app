package com.dhis2.usescases.login;

import com.dhis2.usescases.main.MainActivity;

public class LoginRouter implements LoginContractsModule.Router {

    private LoginContractsModule.View view;

    LoginRouter(LoginContractsModule.View view) {
        this.view = view;
    }

    @Override
    public void navigateToHome() {
        view.startActivity(MainActivity.class, null, true, true, null);
    }
}