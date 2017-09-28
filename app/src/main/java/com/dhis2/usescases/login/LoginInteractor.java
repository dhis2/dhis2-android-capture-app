package com.dhis2.usescases.login;

public class LoginInteractor implements LoginContracts.Interactor {

    private LoginContracts.View view;

    LoginInteractor(LoginContracts.View view) {
        this.view = view;
    }

}