package com.dhis2.usescases.programDetailTablet;

import android.os.Bundle;

import com.dhis2.usescases.main.program.HomeViewModel;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;

import org.hisp.dhis.android.core.program.ProgramModel;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailPresenter implements ProgramDetailContractModule.Presenter {

    private ProgramDetailContractModule.View view;
    private final ProgramDetailContractModule.Interactor interactor;
    private String programId;
    public ProgramModel program;

    @Inject
    ProgramDetailPresenter(ProgramDetailContractModule.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(ProgramDetailContractModule.View mview, String programId) {
        this.programId = programId;
        this.view = mview;
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
    public void nextPageForApi(int page) {
        interactor.getData(page);
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
}
