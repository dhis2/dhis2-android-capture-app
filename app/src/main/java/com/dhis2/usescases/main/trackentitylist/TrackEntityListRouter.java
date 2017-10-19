package com.dhis2.usescases.main.trackentitylist;

public class TrackEntityListRouter implements TrackEntityListContractsModule.Router {

    private TrackEntityListContractsModule.View view;
    private TrackEntityListContractsModule.Interactor interactor;

    TrackEntityListRouter(TrackEntityListContractsModule.View view) {
        this.view = view;
        this.interactor = new TrackEntityListInteractor(view);
    }

}