package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
        if (savedInstanceState != null) {
            teiUid = savedInstanceState.getString(Constants.TRACKED_ENTITY_INSTANCE);
            programUid = savedInstanceState.getString(Constants.PROGRAM_UID);
        } else {
            teiUid = getIntent().getStringExtra("TEI_UID");
            programUid = getIntent().getStringExtra("PROGRAM_UID");
        }
        ((App) getApplicationContext()).userComponent().plus(new TeiDashboardModule(teiUid, programUid)).inject(this);
        super.onCreate(savedInstanceState);

    }

   /* @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        outState.putString("UID", teiUid);
        outState.putString("PROGRAM_ID", programUid);
        super.onSaveInstanceState(outState);
    }*/

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
    public void showCatComboDialog(String eventId, CategoryCombo catCombo) {

    }

}
