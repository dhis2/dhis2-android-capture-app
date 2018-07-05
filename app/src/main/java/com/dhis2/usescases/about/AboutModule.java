package com.dhis2.usescases.about;

import com.dhis2.data.dagger.PerFragment;
import com.dhis2.data.metadata.MetadataRepository;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 05/07/2018.
 */
@Module
public class AboutModule {

    @Provides
    @PerFragment
    AboutPresenter providesPresenter(MetadataRepository metadataRepository) {
        return new AboutPresenterImpl(metadataRepository);
    }
}
