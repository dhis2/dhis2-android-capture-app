package com.dhis2.usescases.appInfo;

import com.dhis2.data.dagger.PerFragment;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 31/01/2018.
 */
@PerFragment
@Module
public class InfoModule {

    @Provides
    @PerFragment
    InfoRepository infoRepository(BriteDatabase briteDatabase) {
        return new InfoRepositoryImpl(briteDatabase);
    }
}
