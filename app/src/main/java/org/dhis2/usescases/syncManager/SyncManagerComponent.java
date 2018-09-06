package org.dhis2.usescases.syncManager;

import org.dhis2.data.dagger.PerFragment;

import dagger.Subcomponent;

/**
 * Created by frodriguez on 4/13/2018.
 */

@PerFragment
@Subcomponent(modules = SyncManagerModule.class)
public interface SyncManagerComponent {
    void inject(SyncManagerFragment syncManagerFragment);
}
