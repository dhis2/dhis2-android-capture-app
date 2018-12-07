package org.dhis2.usescases.synchronization;

import org.dhis2.data.dagger.PerActivity;


import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = SynchronizationModule.class)
public interface SynchronizationComponent {
    void inject(SynchronizationActivity synchronizationActivity);
}