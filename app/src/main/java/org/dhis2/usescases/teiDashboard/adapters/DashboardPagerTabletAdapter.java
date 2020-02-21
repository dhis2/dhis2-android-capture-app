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
import org.jetbrains.annotations.NotNull;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class DashboardPagerTabletAdapter extends FragmentStatePagerAdapter {

    private static final int MOBILE_DASHBOARD_SIZE = 3;
    private final Context context;
    private String currentProgram;
    private final int NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED = 0;


    public DashboardPagerTabletAdapter(Context context, FragmentManager fm, String program) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.currentProgram = program;
        this.context = context;
    }

    @Override
    public Parcelable saveState() {
        // Do Nothing
        return null;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            default:
                return new IndicatorsFragment();
            case 1:
                return new RelationshipFragment();
            case 2:
                return new NotesFragment();
        }
    }

    @Override
    public int getCount() {
        return currentProgram != null ? MOBILE_DASHBOARD_SIZE : NO_FRAGMENT_DUE_TO_NO_PROGRAM_SELECTED;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(currentProgram!=null) {
            switch (position) {
                default:
                    return context.getString(R.string.dashboard_indicators);
                case 1:
                    return context.getString(R.string.dashboard_relationships);
                case 2:
                    return context.getString(R.string.dashboard_notes);
            }
        }else{
            return null;
        }
    }
}
