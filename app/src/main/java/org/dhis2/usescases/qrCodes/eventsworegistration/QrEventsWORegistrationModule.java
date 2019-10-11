package org.dhis2.usescases.qrCodes.eventsworegistration;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.qr.QRCodeGenerator;
import org.dhis2.data.qr.QRInterface;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */
@PerActivity
@Module
public class QrEventsWORegistrationModule {
    @Provides
    @PerActivity
    QrEventsWORegistrationContracts.View provideView(QrEventsWORegistrationActivity qrActivity) {
        return qrActivity;
    }

    @Provides
    @PerActivity
    QrEventsWORegistrationContracts.Presenter providePresenter(QRInterface qrInterface, SchedulerProvider schedulerProvider) {
        return new QrEventsWORegistrationPresenter(qrInterface, schedulerProvider);
    }

    @Provides
    @PerActivity
    QRInterface providesQRInterface(BriteDatabase briteDatabase, D2 d2) {
        return new QRCodeGenerator(briteDatabase,d2);
    }

}
