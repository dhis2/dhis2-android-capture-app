package com.dhis2.usescases.teiDashboard;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.App;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 26/04/2018.
 */

public class TeiDashboardActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    @Inject
    public TeiDashboardContracts.Presenter presenter;

    public DashboardProgramModel programModel;
    public DashboardPagerAdapter adapter;

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

    }

    @Override
    public String getToolbarTitle() {
        return null;
    }

    @Override
    public DashboardPagerAdapter getAdapter() {
        return null;
    }

    @Override
    public Consumer<List<Bitmap>> showQR() {
        return data -> {
        };
    }

    @Override
    public void nextQR() {

    }

    @Override
    public void goToEnrollmentList(Bundle extras) {

    }


}
