package org.dhis2.usescases.sync;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
@PerActivity
public class SyncModule {

    @Provides
    @PerActivity
    SyncContracts.Presenter providePresenter() {
        return new SyncPresenter();
    }
}
