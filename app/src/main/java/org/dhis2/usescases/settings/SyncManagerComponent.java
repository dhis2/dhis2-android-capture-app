package org.dhis2.usescases.settings;

import org.dhis2.commons.di.dagger.PerFragment;

import dagger.Subcomponent;


@PerFragment
@Subcomponent(modules = SyncManagerModule.class)
public interface SyncManagerComponent {
    void inject(SyncManagerFragment syncManagerFragment);
}
