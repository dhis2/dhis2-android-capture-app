package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.dhis2.App;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

/**
 * QUADRAM. Created by ppajuelo on 26/04/2018.
 */

public class TeiDashboardActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    @Inject
    public TeiDashboardContracts.Presenter presenter;

    protected DashboardProgramModel programModel;

    protected String teiUid;
    protected String programUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new TeiDashboardModule()).inject(this);
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            teiUid = savedInstanceState.getString("UID");
            programUid = savedInstanceState.getString("PROGRAM_ID");
        } else {
            teiUid = getIntent().getStringExtra("TEI_UID");
            programUid = getIntent().getStringExtra("PROGRAM_UID");
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("UID", teiUid);
        outState.putString("PROGRAM_ID", programUid);
    }

    @Override
    public void init(String teUid, String programUid) {
        // nothing
    }

    @Override
    public void setData(DashboardProgramModel program) {
        // nothing
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
        // nothing
    }

    @Override
    public void showCatComboDialog(String eventId, String programStage, List<CategoryOptionComboModel> catComboOptions, String title) {
        // nothing
    }
}
