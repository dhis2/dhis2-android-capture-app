package org.dhis2.usescases.teiDashboard.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class DashboardPagerTabletAdapter extends FragmentStatePagerAdapter {

    private static final int MOVILE_DASHBOARD_SIZE = 3;
    private String currentProgram;


    public DashboardPagerTabletAdapter(FragmentManager fm, String program) {
        super(fm);
        this.currentProgram = program;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            default:
                return RelationshipFragment.getInstance();
            case 1:
                return IndicatorsFragment.getInstance();
            case 2:
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
                return "Overview";
            case 1:
                return "Relationships";
            case 2:
                return "Indicators";
            case 3:
                return "Notes";
        }
    }
}
