package com.dhis2.usescases.main;

public class MainInteractor implements MainContractsModule.Interactor {

    private MainContractsModule.View view;

    MainInteractor(MainContractsModule.View view) {
        this.view = view;
    }


}