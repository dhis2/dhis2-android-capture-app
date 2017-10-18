package com.dhis2.usescases.main;

public class MainInteractor implements MainContracts.Interactor {

    private MainContracts.View view;

    MainInteractor(MainContracts.View view) {
        this.view = view;
    }


}