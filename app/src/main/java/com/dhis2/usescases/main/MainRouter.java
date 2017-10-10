package com.dhis2.usescases.main;

public class MainRouter implements MainContractsModule.Router {

    private MainContractsModule.View view;

    MainRouter(MainContractsModule.View view) {
        this.view = view;
    }

}