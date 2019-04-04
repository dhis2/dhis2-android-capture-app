package org.dhis2.usescases.teiDashboard.adapters;

import android.content.Context;

import org.dhis2.R;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.notes.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment;
import org.jetbrains.annotations.NotNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class DashboardPagerAdapter extends FragmentStatePagerAdapter {

    private static final int MOVILE_DASHBOARD_SIZE = 4;
    private String currentProgram;
    private String tei;
    private Context context;


    public DashboardPagerAdapter(Context context, FragmentManager fm, String program, String tei) {
        super(fm);
        this.context = context;
        this.currentProgram = program;
        this.tei = tei;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new IndicatorsFragment();
            case 2:
                return RelationshipFragment.createInstance();
            case 3:
                return new NotesFragment();
            default:
                return TEIDataFragment.createInstance();

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
                return context.getString(R.string.dashboard_indicators);
            case 2:
                return context.getString(R.string.dashboard_relationships);
            case 3:
                return context.getString(R.string.dashboard_notes);
        }
    }
}
