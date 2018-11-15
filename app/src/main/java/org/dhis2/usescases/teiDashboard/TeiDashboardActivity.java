package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.dhis2.App;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

/**
 * QUADRAM. Created by ppajuelo on 26/04/2018.
 */

public class TeiDashboardActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    @Inject
    public TeiDashboardContracts.Presenter presenter;

    public DashboardProgramModel programModel;

    public String teiUid;
    public String programUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new TeiDashboardModule()).inject(this);
        super.onCreate(savedInstanceState);
        teiUid = getIntent().getStringExtra("TEI_UID");
        programUid = getIntent().getStringExtra("PROGRAM_UID");
    }

    @Override
    public void init(String teUid, String programUid) {

    }

    @Override
    public void setData(DashboardProgramModel program) {

    }

    @Override
    public void setDataWithOutProgram(DashboardProgramModel programModel) {
        // unused
    }

    @Override
    public String getToolbarTitle() {
        return null;
    }

    @Override
    public FragmentStatePagerAdapter getAdapter() {
        return null;
    }

    @Override
    public void showQR() {
        // unused
    }

    @Override
    public void goToEnrollmentList(Bundle extras) {
        // unused
    }

    @Override
    public void restoreAdapter(String programUid) {

    }
}
