package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.indicators.EventIndicatorsComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.indicators.EventIndicatorsModule;

import androidx.annotation.NonNull;
import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
@PerActivity
@Subcomponent(modules = EventCaptureModule.class)
public interface EventCaptureComponent {
    @NonNull
    EventIndicatorsComponent plus(EventIndicatorsModule eventIndicatorsModule);

    void inject(EventCaptureActivity activity);
}
