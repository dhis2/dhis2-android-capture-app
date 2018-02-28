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
        if (dashboardProgram.getCurrentProgram().relationshipType() != null) {
            pagerFragments.add(RelationshipFragment.getInstance());
            pagerFragmentsTitle.add("Relationships");
        }
        pagerFragments.add(IndicatorsFragment.getInstance());
        pagerFragmentsTitle.add("Indicators");

        pagerFragments.add(ScheduleFragment.getInstance());
        pagerFragmentsTitle.add("Schedule");

        pagerFragments.add(NotesFragment.getInstance());
        pagerFragmentsTitle.add("Notes");

    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        int positionfix = isTablet ? 1 : 0;
        switch (position + positionfix) {
            default:
                fragment = TEIDataFragment.getInstance();
                break;
            case 1:
                fragment = RelationshipFragment.getInstance();
                break;
            case 2:
                fragment = IndicatorsFragment.getInstance();
                break;
            case 3:
                fragment = ScheduleFragment.getInstance();
                break;
            case 4:
                fragment = NotesFragment.getInstance();
                break;
        }
        return pagerFragments.get(position);
    }

    @Override
    public int getCount() {
        return pagerFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position) {
            default:
                title = "Program";
                break;
            case 1:
                title = "Relationships";
                break;
            case 2:
                title = "Indicators";
                break;
            case 3:
                title = "Schedule";
                break;
            case 4:
                title = "Notes";
                break;
        }
        return pagerFragmentsTitle.get(position);
    }
}
