package com.dhis2.usescases.main;

public class MainRouter implements MainContracts.Router {

    private MainContracts.View view;

    MainRouter(MainContracts.View view) {
        this.view = view;
    }

}