package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsComponent;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsModule;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = EventCaptureModule.class)
public interface EventCaptureComponent {
    void inject(EventCaptureActivity activity);
// TODO: reversed
    // void inject(TeiDashboardMobileActivity teiDashboardMobileActivity);

    EventCaptureFormComponent plus(EventCaptureFormModule formModule);

    IndicatorsComponent plus(IndicatorsModule indicatorsModule);

    EventDetailsComponent plus(EventDetailsModule eventDetailsModule);
}
