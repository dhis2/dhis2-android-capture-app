package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.content.Context;

import org.dhis2.R;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.notes.NotesFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
                return EventCaptureFormFragment.getInstance();
            case 1:
                return NotesFragment.getEventInstance(programUid, eventUid);
        }
    }

    @Override
    public int getCount() {
        return 2; //TODO: ADD OVERVIEW, INDICATORS
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            default:
                return context.getString(R.string.event_overview);
            case 1:
                return context.getString(R.string.event_notes);
        }
    }
}
