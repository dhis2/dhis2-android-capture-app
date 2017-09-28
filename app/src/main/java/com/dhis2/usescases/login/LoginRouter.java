package com.dhis2.usescases.login;

public class LoginRouter implements LoginContracts.Router {

    private LoginContracts.View view;
    private LoginContracts.Interactor interactor;

    LoginRouter(LoginContracts.View view) {
        this.view = view;
        this.interactor = new LoginInteractor(view);
    }

}