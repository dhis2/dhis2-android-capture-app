package org.dhis2.usescases.synchronization;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;

import dagger.Module;
import dagger.Provides;

@Module
@PerActivity
public class SynchronizationModule {

    @Provides
    @PerActivity
    SynchronizationContracts.Presenter providePresenter(MetadataRepository metadataRepository) {
        return new SynchronizationPresenter(metadataRepository);
    }
}
