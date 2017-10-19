package com.dhis2.usescases.main.trackentitylist;


public class TrackEntityListInteractor implements TrackEntityListContractsModule.Interactor {

    private TrackEntityListContractsModule.View view;


    TrackEntityListInteractor(TrackEntityListContractsModule.View view) {
        this.view = view;
    }

}