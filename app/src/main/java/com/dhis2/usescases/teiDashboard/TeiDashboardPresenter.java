package com.dhis2.usescases.teiDashboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.dhis2.usescases.eventDetail.EventDetailActivity;
import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailActivity;

import org.hisp.dhis.android.core.program.ProgramModel;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter {

    private TeiDashboardContracts.View view;

    @NonNull
    private final TeiDashboardContracts.Interactor interactor;

    private String teUid;
    private String programUid;

    public TeiDashboardPresenter(TeiDashboardContracts.Interactor interactor) {
        this.interactor = interactor;
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
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        view.startActivity(TeiDataDetailActivity.class, extras, false, false, options);
    }

    @Override
    public void onEventSelected(String uid, View sharedView) {
        Bundle extras = new Bundle();
        extras.putString("EVENT_UID", uid);
        extras.putString("TOOLBAR_TITLE", view.getToolbarTitle());
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
        view.startActivity(EventDetailActivity.class, extras, false, false, options);
    }
}
