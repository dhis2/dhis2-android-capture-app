package com.dhis2.usescases.teiDashboard;

import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailActivity;

import org.hisp.dhis.android.core.program.ProgramModel;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private TeiDashboardContracts.View view;

    @Inject
    TeiDashboardInteractor interactor;
    private String teUid;
    private String programUid;

    @Inject
    public TeiDashboardPresenter() {

    }

    @Override
    public void init(TeiDashboardContracts.View view, String teiUid, String programUid) {
        this.view = view;
        this.teUid = teiUid;
        this.programUid = programUid;
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
    public void onEnrollmentSelectorClick() {
        interactor.getEnrollments(teUid);
    }

    @Override
    public void setProgram(ProgramModel program) {
    }

    @Override
    public void editTei(boolean isEditable, View sharedView) {
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        extras.putString("PROGRAM_UID", programUid);
        extras.putBoolean("IS_EDITABLE", isEditable);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(),sharedView,"user_info");
        view.startActivity(TeiDataDetailActivity.class, extras, false, false, options);
    }
}
