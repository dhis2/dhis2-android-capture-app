package com.dhis2.usescases.splash;

import com.data.server.UserManager;

import javax.inject.Inject;

public class SplashPresenter implements SplashContractsModule.Presenter {

    private SplashContractsModule.View view;
    private SplashContractsModule.Interactor interactor;

    @Inject
    SplashPresenter(SplashContractsModule.View view) {
        this.view = view;
        this.interactor = new SplashInteractor(view);
    }

    public void destroy() {
        interactor.destroy();
    }

}