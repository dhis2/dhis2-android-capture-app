package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;
import android.os.Bundle;

import org.dhis2.R;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.notes.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VisualizationType;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import static org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragmentKt.VISUALIZATION_TYPE;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCapturePagerAdapter extends FragmentStatePagerAdapter {

    private final Context context;
    private final String programUid;
    private final String eventUid;

    public EventCapturePagerAdapter(FragmentManager fm, Context context,String programUid, String eventUid) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
        this.programUid = programUid;
        this.eventUid = eventUid;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            default:
                return EventCaptureFormFragment.newInstance(eventUid);
            case 1:
                Fragment indicatorFragment =new IndicatorsFragment();
                Bundle arguments = new Bundle();
                arguments.putString(VISUALIZATION_TYPE, VisualizationType.EVENTS.name());
                indicatorFragment.setArguments(arguments);
                return indicatorFragment;
            case 2:
                return NotesFragment.newEventInstance(programUid, eventUid);
        }
    }

    @Override
    public int getCount() {
        return 3; //TODO: ADD OVERVIEW
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            default:
                return context.getString(R.string.event_data);
            case 1:
                return context.getString(R.string.dashboard_indicators);
            case 2:
                return context.getString(R.string.event_notes);
        }
    }
}
