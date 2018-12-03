package org.dhis2.usescases.sync;

import org.dhis2.data.dagger.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
@PerActivity
public class SyncModule {
    @Provides
    @PerActivity
    SyncContracts.Presenter presenter() {
        return new SyncPresenterImpl();
    }
}
