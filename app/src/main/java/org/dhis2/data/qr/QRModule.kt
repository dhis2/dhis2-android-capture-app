package org.dhis2.data.qr

import com.squareup.sqlbrite2.BriteDatabase

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */
@Module
class QRModule {

    @Provides
    @Singleton
    internal fun provideMetadataRepository(briteDatabase: BriteDatabase): QRInterface {
        return QRCodeGenerator(briteDatabase, null)
    }
}
