package com.dhis2.usescases.splash;


import javax.inject.Inject;

public class SplashPresenter implements SplashContractsModule.Presenter {

    private SplashContractsModule.View view;
    private SplashContractsModule.Interactor interactor;

    @Inject
    SplashPresenter(SplashContractsModule.View view, SplashContractsModule.Interactor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    public void destroy() {
        interactor.destroy();
    }

}