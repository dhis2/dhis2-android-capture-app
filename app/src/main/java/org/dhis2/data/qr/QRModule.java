package org.dhis2.data.qr;

import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */
@Module
public class QRModule {

    @Provides
    @Singleton
    QRInterface provideRepository(BriteDatabase briteDatabase) {
        return new QRCodeGenerator(briteDatabase, null);
    }
}
