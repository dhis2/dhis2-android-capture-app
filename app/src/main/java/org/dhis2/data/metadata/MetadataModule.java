package org.dhis2.data.metadata;

import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

@Module
public class MetadataModule {

    @Provides
    @Singleton
    MetadataRepository provideMetadataRepository(BriteDatabase briteDatabase) {
        return new MetadataRepositoryImpl(briteDatabase);
    }

}
