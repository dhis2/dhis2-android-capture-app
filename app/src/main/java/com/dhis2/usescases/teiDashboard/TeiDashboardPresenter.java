package com.dhis2.usescases.teiDashboard;

import org.hisp.dhis.android.core.program.ProgramModel;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private TeiDashboardContracts.View view;

    @Inject
    TeiDashboardInteractor interactor;

    @Inject
    public TeiDashboardPresenter() {

    }

    @Override
    public void init(TeiDashboardContracts.View view, String teiUid, String programUid) {
        this.view = view;
        interactor.init(view, teiUid, programUid);
    }

    @Override
    public void onBackPressed() {
        view.back();
    }

    @Override
    public void onProgramSelected() {

    }

    @Override
    public void setProgram(ProgramModel program) {
    }
}
