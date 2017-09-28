package com.dhis2.usescases.splash;

public class SplashRouter implements SplashContracts.Router {

    private SplashContracts.View view;
    private SplashContracts.Interactor interactor;

    SplashRouter(SplashContracts.View view) {
        this.view = view;
        this.interactor = new SplashInteractor(view);
    }

    @Override
    public void navigateToLoginView() {

    }

    @Override
    public void navigateToHomeView() {

    }
}