package org.dhis2.usescases.sync;

import org.dhis2.data.dagger.PerActivity;


import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = SyncModule.class)
public interface SyncComponent {
    void inject(SyncActivity syncActivity);
}