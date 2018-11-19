package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;

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
            default:
                return EventCaptureFormFragment.getInstance();
        }
    }

    @Override
    public int getCount() {
        return 1; //TODO: ADD OVERVIEW, INDICATORS, NOTES
    }
}
