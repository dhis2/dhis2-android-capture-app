package com.dhis2.usescases.main.trackentitylist;

import javax.inject.Inject;

public class TrackEntityListPresenter implements TrackEntityListContractsModule.Presenter {

    private TrackEntityListContractsModule.View view;
    private TrackEntityListContractsModule.Interactor interactor;

    @Inject
    TrackEntityListPresenter(TrackEntityListContractsModule.View view) {
        this.view = view;
        this.interactor = new TrackEntityListInteractor(view);
    }

    @Override
    public void loadTrackEntities() {

    }
}