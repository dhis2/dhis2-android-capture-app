package com.dhis2.usescases.programDetail;

import android.os.Bundle;

import com.dhis2.usescases.main.program.HomeViewModel;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailPresenter implements ProgramDetailContractModule.Presenter {

    static private ProgramDetailContractModule.View view;
    private final ProgramDetailContractModule.Interactor interactor;
    public HomeViewModel program;

    @Inject
    ProgramDetailPresenter(ProgramDetailContractModule.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(ProgramDetailContractModule.View mview, HomeViewModel program) {
        this.program = program;
        view = mview;
        interactor.init(view, program.id());
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
    public HomeViewModel getCurrentProgram() {
        return program;
    }

    @Override
    public void nextPageForApi(int page) {
        interactor.getData(page);
    }

    @Override
    public void onSearchClick() {
        Bundle bundle = new Bundle();
        bundle.putString("TRACKED_ENTITY_UID", program.trackedEntityType());
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
