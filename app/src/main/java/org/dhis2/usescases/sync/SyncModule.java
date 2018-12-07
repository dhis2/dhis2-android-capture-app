package org.dhis2.usescases.sync;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;

import dagger.Module;
import dagger.Provides;

@Module
@PerActivity
public class SynchronizationModule {

    @Provides
    @PerActivity
    SyncContracts.Presenter providePresenter(MetadataRepository metadataRepository) {
        return new SynchronizationPresenter(metadataRepository);
    }
}
