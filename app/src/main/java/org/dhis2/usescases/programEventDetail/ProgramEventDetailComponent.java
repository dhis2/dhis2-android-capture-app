package org.dhis2.usescases.programEventDetail;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.usescases.programEventDetail.eventList.EventListComponent;
import org.dhis2.usescases.programEventDetail.eventList.EventListModule;
import org.dhis2.usescases.programEventDetail.eventMap.EventMapComponent;
import org.dhis2.usescases.programEventDetail.eventMap.EventMapModule;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = ProgramEventDetailModule.class)
public interface ProgramEventDetailComponent {
    void inject(ProgramEventDetailActivity activity);

    EventListComponent plus(EventListModule eventListModule);

    EventMapComponent plus(EventMapModule eventMapModule);
}