package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.notes.NotesFragment;

public class EventCapturePagerAdapter extends FragmentStateAdapter {

    private final String programUid;
    private final String eventUid;

    public EventCapturePagerAdapter(FragmentActivity fragmentActivity,
                                    String programUid,
                                    String eventUid) {
        super(fragmentActivity);
        this.programUid = programUid;
        this.eventUid = eventUid;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return NotesFragment.newEventInstance(programUid, eventUid);
        }
        return EventCaptureFormFragment.newInstance(eventUid);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
