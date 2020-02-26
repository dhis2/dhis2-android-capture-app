package org.dhis2.usescases.teiDashboard.adapters;

import android.content.Context;
import android.os.Parcelable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.dhis2.R;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment;
import org.dhis2.usescases.notes.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataFragment;
import org.jetbrains.annotations.NotNull;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class DashboardPagerAdapter extends FragmentStatePagerAdapter {

    private static final int MOBILE_DASHBOARD_SIZE = 4;
    private final String enrollmentUid;
    private String currentProgram;
    private final String teiUid;
    private Context context;


    public DashboardPagerAdapter(Context context, FragmentManager fm, String program, String teiUid, String enrollmentUid) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
        this.currentProgram = program;
        this.teiUid = teiUid;
        this.enrollmentUid = enrollmentUid;
    }

    @Override
    public Parcelable saveState() {
        // Do Nothing
        return null;
    }

    private IndicatorsFragment indicatorsFragment;
    private RelationshipFragment relationshipFragment;
    private TEIDataFragment teiDataFragment;

    @NotNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                if (indicatorsFragment == null) {
                    indicatorsFragment = new IndicatorsFragment();
                }
                return indicatorsFragment;
            case 2:
                if (relationshipFragment == null) {
                    relationshipFragment = new RelationshipFragment();
                }
                return relationshipFragment;
            case 3:
                return NotesFragment.newTrackerInstance(currentProgram, teiUid);
            default:
                return TEIDataFragment.newInstance(currentProgram, teiUid, enrollmentUid);
        }
    }

    @Override
    public int getCount() {
        return currentProgram != null ? MOBILE_DASHBOARD_SIZE : 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            default:
                return context.getString(R.string.dashboard_overview);
            case 1:
                return context.getString(R.string.dashboard_indicators);
            case 2:
                return context.getString(R.string.dashboard_relationships);
            case 3:
                return context.getString(R.string.dashboard_notes);
        }
    }
}
