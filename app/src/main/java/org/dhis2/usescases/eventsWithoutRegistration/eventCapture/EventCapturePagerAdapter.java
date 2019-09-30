package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventInitialFragment.EventCaptureInitialFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCapturePagerAdapter extends FragmentStatePagerAdapter {

    public EventCapturePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new EventCaptureInitialFragment();
            default:
                return EventCaptureFormFragment.getInstance();
        }
    }

    @Override
    public int getCount() {
        return 2; //TODO: ADD OVERVIEW, INDICATORS, NOTES
    }
}
