package org.dhis2.usescases.teiDashboard.adapters;

import android.content.Context;

import org.dhis2.R;
import org.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class DashboardPagerTabletAdapter extends FragmentStatePagerAdapter {

    private static final int MOVILE_DASHBOARD_SIZE = 3;
    private final Context context;
    private String currentProgram;


    public DashboardPagerTabletAdapter(Context context, FragmentManager fm, String program) {
        super(fm);
        this.currentProgram = program;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return RelationshipFragment.getInstance();
            case 2:
                return NotesFragment.getInstance();
            default:
                return IndicatorsFragment.getInstance();
        }
    }

    @Override
    public int getCount() {
        return currentProgram != null ? MOVILE_DASHBOARD_SIZE : 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 1:
                return context.getString(R.string.dashboard_relationships);
            case 2:
                return context.getString(R.string.dashboard_notes);
            default:
                return context.getString(R.string.dashboard_indicators);
        }
    }
}
