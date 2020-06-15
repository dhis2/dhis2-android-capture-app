package org.dhis2.usescases.qrReader;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

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
    QrReaderContracts.Presenter providePresenter(D2 d2, SchedulerProvider schedulerProvider) {
        return new QrReaderPresenterImpl(d2, schedulerProvider);
    }
}
