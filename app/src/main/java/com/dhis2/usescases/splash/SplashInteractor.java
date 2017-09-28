package com.dhis2.usescases.splash;

public class SplashInteractor implements SplashContracts.Interactor {

    private SplashContracts.View view;

    SplashInteractor(SplashContracts.View view) {
        this.view = view;
    }

}