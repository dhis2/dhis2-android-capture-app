package com.dhis2.usescases.appInfo;

import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 31/01/2018.
 */

@Module
public class InfoModule {

    @Provides
    InfoRepository infoRepository(BriteDatabase briteDatabase) {
        return new InfoRepositoryImpl(briteDatabase);
    }
}
