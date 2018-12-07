package org.dhis2.usescases.sync;

import org.dhis2.data.dagger.PerActivity;


import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = SynchronizationModule.class)
public interface SynchronizationComponent {
    void inject(SyncActivity syncActivity);
}