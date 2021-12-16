package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.notes.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VisualizationType;

import static org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragmentKt.VISUALIZATION_TYPE;

public class EventCapturePagerAdapter extends FragmentStateAdapter {

    private final String programUid;
    private final String eventUid;

    public EventCapturePagerAdapter(FragmentActivity fragmentActivity, String programUid, String eventUid) {
        super(fragmentActivity);
        this.programUid = programUid;
        this.eventUid = eventUid;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            default:
                return EventCaptureFormFragment.newInstance(eventUid);
            case 1:
                Fragment indicatorFragment = new IndicatorsFragment();
                Bundle arguments = new Bundle();
                arguments.putString(VISUALIZATION_TYPE, VisualizationType.EVENTS.name());
                indicatorFragment.setArguments(arguments);
                return indicatorFragment;
            case 2:
                return NotesFragment.newEventInstance(programUid, eventUid);
        }
    }

    @Override
    public int getItemCount() {
        return 3; //TODO: ADD OVERVIEW
    }
}
