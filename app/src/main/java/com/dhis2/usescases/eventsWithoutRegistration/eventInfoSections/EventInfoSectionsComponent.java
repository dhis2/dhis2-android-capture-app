package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import com.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

@PerActivity
@Subcomponent(modules = EventInfoSectionsModule.class)
public interface EventInfoSectionsComponent {
    void inject(EventInfoSectionsActivity activity);
}