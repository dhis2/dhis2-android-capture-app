package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
@PerActivity
@Subcomponent(modules = EventCaptureModule.class)
public interface EventCaptureComponent {
    void inject(EventCaptureActivity activity);
}
