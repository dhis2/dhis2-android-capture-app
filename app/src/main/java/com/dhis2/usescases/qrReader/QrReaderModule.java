package com.dhis2.usescases.qrReader;

import com.dhis2.data.dagger.PerFragment;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */
@Module
@PerFragment
public class QrReaderModule {

    @Provides
    @PerFragment
    QrReaderContracts.Presenter providePresenter(BriteDatabase briteDatabase) {
        return new QrReaderPresenterImpl(briteDatabase);
    }
}
