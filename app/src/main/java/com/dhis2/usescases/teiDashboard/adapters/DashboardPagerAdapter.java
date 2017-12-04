package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.NotesFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.ScheduleFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class DashboardPagerAdapter extends FragmentStatePagerAdapter {

    private boolean isTablet;

    public DashboardPagerAdapter(FragmentManager fm, boolean isTablet) {
        super(fm);
        this.isTablet = isTablet;
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
        return fragment;
    }

    @Override
    public int getCount() {
        return isTablet ? 4 : 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position){
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
        return title;
    }
}
