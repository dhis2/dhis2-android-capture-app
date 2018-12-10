package org.dhis2.usescases.teiDashboard.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.dhis2.R;
import org.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class DashboardPagerAdapter extends FragmentStatePagerAdapter {

    private static final int MOVILE_DASHBOARD_SIZE = 4;
    private String currentProgram;
    private Context context;


    public DashboardPagerAdapter(Context context, FragmentManager fm, String program) {
        super(fm);
        this.context = context;
        this.currentProgram = program;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            default:
                return TEIDataFragment.getInstance();
            case 1:
                return RelationshipFragment.getInstance();
            case 2:
                return IndicatorsFragment.getInstance();
            case 3:
                return NotesFragment.getInstance();

        }
    }

    @Override
    public int getCount() {
        return currentProgram != null ? MOVILE_DASHBOARD_SIZE : 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            default:
                return context.getString(R.string.dashboard_overview);
            case 1:
                return context.getString(R.string.dashboard_relationships);
            case 2:
                return context.getString(R.string.dashboard_indicators);
            case 3:
                return context.getString(R.string.dashboard_notes);
        }
    }
}
