package com.dhis2.usescases.teiDashboard.teiProgramList;

import java.util.Date;

/**
 * Created by Cristian on 06/03/2018.
 *
 */

public class TeiProgramListPresenter implements TeiProgramListContract.Presenter {

    private TeiProgramListContract.View view;
    private final TeiProgramListContract.Interactor interactor;

    TeiProgramListPresenter(TeiProgramListContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(TeiProgramListContract.View view, String trackedEntityId) {
        this.view = view;
        interactor.init(view, trackedEntityId);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onEnrollClick(String enrollmentId) {
        // TODO CRIS
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }

    @Override
    public void filterOrgUnits(Date date) {

    }
}
