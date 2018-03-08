package com.dhis2.usescases.teiDashboard.teiProgramList;

/**
 * Created by Cristian on 06/03/2018.
 *
 */

public class TeiProgramListPresenter implements TeiProgramListContract.Presenter {

    private TeiProgramListContract.View view;
    private final TeiProgramListContract.Interactor interactor;

    private String trackedEntityId;
    private String programId;

    TeiProgramListPresenter(TeiProgramListContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(TeiProgramListContract.View view, String trackedEntityId, String programId) {
        this.view = view;
        this.trackedEntityId = trackedEntityId;
        this.programId = programId;
        interactor.init(view, trackedEntityId, programId);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onEnrollmentClick(String enrollmentId) {
        // TODO CRIS
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }
}
