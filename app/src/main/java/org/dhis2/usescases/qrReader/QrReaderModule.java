package org.dhis2.usescases.qrReader;

import org.dhis2.commons.di.dagger.PerFragment;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 22/05/2018.
 */
@Module
public class QrReaderModule {

    @Provides
    @PerFragment
    QrReaderContracts.Presenter providePresenter(D2 d2, SchedulerProvider schedulerProvider) {
        return new QrReaderPresenterImpl(d2, schedulerProvider);
    }
}
