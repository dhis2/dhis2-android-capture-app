package org.dhis2.usescases.teiDashboard.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

import java.util.ArrayList;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class DashboardPagerAdapter extends FragmentStatePagerAdapter {

    private boolean isTablet;
    private DashboardProgramModel dashboardProgram;

    private ArrayList<Fragment> pagerFragments = new ArrayList<>();
    private ArrayList<String> pagerFragmentsTitle = new ArrayList<>();

    public DashboardPagerAdapter(FragmentManager fm, DashboardProgramModel program, boolean isTablet) {
        super(fm);
        this.isTablet = isTablet;
        this.dashboardProgram = program;

        if (!isTablet) {
            pagerFragments.add(TEIDataFragment.createInstance());
            pagerFragmentsTitle.add("Overview");
        }
        if (program.getCurrentProgram() != null) {
            pagerFragments.add(RelationshipFragment.createInstance());
            pagerFragmentsTitle.add("Relationships");

            pagerFragments.add(IndicatorsFragment.createInstance());
            pagerFragmentsTitle.add("Indicators");

            pagerFragments.add(NotesFragment.createInstance());
            pagerFragmentsTitle.add("Notes");
        }

    }

    @Override
    public Fragment getItem(int position) {
        return pagerFragments.get(position);
    }

    @Override
    public int getCount() {
        return pagerFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pagerFragmentsTitle.get(position);
    }
}
