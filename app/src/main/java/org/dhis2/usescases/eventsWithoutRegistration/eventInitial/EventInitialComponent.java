package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import org.dhis2.commons.di.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = EventInitialModule.class)
public interface EventInitialComponent {
    void inject(EventInitialActivity activity);
    EventDetailsComponent plus(EventDetailsModule eventDetailsModule);
}