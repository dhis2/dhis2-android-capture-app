package com.dhis2.usescases.splash;

public class SplashPresenter implements SplashContracts.Presenter {

    private SplashContracts.View view;
    private SplashContracts.Interactor interactor;


    SplashPresenter(SplashContracts.View view) {
        this.view = view;
        this.interactor = new SplashInteractor(view);
    }

    @Override
    public void isUserLoggedIn() {

    }


}