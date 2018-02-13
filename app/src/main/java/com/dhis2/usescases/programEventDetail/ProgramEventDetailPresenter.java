package com.dhis2.usescases.programEventDetail;

import android.os.Bundle;

import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import org.hisp.dhis.android.core.program.ProgramModel;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    static private ProgramEventDetailContract.View view;
    private final ProgramEventDetailContract.Interactor interactor;
    private String programId;
    public ProgramModel program;

    ProgramEventDetailPresenter(ProgramEventDetailContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(ProgramEventDetailContract.View mview, String programId) {
        this.programId = programId;
        view = mview;
        interactor.init(view, programId);
    }

    @Override
    public void onTimeButtonClick() {
        view.showTimeUnitPicker();
    }

    @Override
    public void onDateRangeButtonClick() {
        view.showRageDatePicker();
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
    }

    @Override
    public void onCatComboButtonClick() {

    }

    @Override
    public ProgramModel getCurrentProgram() {
        return program;
    }

    @Override
    public void setProgram(ProgramModel program) {
        this.program = program;
    }

    @Override
    public void onSearchClick() {
        Bundle bundle = new Bundle();
        //bundle.putString("TRACKED_ENTITY_UID", program.trackedEntityType());
        view.startActivity(SearchTEActivity.class, bundle, false, false, null);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }

    @Override
    public void onTEIClick(String TEIuid, String programUid) {
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", TEIuid);
        bundle.putString("PROGRAM_UID", programUid);
        view.startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
    }
}
