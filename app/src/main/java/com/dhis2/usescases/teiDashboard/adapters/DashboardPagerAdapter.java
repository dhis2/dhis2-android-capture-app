package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.ScheduleFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

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
            pagerFragments.add(TEIDataFragment.getInstance());
            pagerFragmentsTitle.add("Program");
        }
        if (program.getCurrentProgram() != null) {
            pagerFragments.add(RelationshipFragment.getInstance());
            pagerFragmentsTitle.add("Relationships");

            pagerFragments.add(IndicatorsFragment.getInstance());
            pagerFragmentsTitle.add("Indicators");

            pagerFragments.add(ScheduleFragment.getInstance());
            pagerFragmentsTitle.add("Schedule");

            pagerFragments.add(NotesFragment.getInstance());
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
