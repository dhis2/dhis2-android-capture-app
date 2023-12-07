package org.dhis2.usescases.eventswithoutregistration.eventcapture;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.usescases.eventswithoutregistration.eventcapture.eventcapturefragment.EventCaptureFormComponent;
import org.dhis2.usescases.eventswithoutregistration.eventcapture.eventcapturefragment.EventCaptureFormModule;
import org.dhis2.usescases.eventswithoutregistration.eventDetails.injection.EventDetailsComponent;
import org.dhis2.usescases.eventswithoutregistration.eventDetails.injection.EventDetailsModule;
import org.dhis2.usescases.teidashboard.dashboardfragments.indicators.IndicatorsComponent;
import org.dhis2.usescases.teidashboard.dashboardfragments.indicators.IndicatorsModule;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = EventCaptureModule.class)
public interface EventCaptureComponent {
    void inject(EventCaptureActivity activity);

    EventCaptureFormComponent plus(EventCaptureFormModule formModule);

    IndicatorsComponent plus(IndicatorsModule indicatorsModule);

    EventDetailsComponent plus(EventDetailsModule eventDetailsModule);
}