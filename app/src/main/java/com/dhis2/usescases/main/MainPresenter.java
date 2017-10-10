package com.dhis2.usescases.main;

import javax.inject.Inject;

public class MainPresenter implements MainContractsModule.Presenter {

    private MainContractsModule.View view;
    private MainContractsModule.Interactor interactor;

    @Inject
    MainPresenter(MainContractsModule.View view, HomeRepository homeRepository) {
        this.view = view;
        this.interactor = new MainInteractor(view, homeRepository);
    }

    public void init(){
        interactor.getData();
    }

}