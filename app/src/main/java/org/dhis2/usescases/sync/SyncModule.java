package org.dhis2.usescases.sync;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;

import dagger.Module;
import dagger.Provides;

@Module
@PerActivity
public class SyncModule {

    @Provides
    @PerActivity
    SyncContracts.SyncPresenter providePresenter(MetadataRepository metadataRepository) {
        return new SyncPresenterImpl(metadataRepository);
    }
}
