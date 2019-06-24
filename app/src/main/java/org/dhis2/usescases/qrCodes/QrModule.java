package org.dhis2.usescases.qrCodes;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.qr.QRCodeGenerator;
import org.dhis2.data.qr.QRInterface;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */
@PerActivity
@Module
public class QrModule {
    @Provides
    @PerActivity
    QrContracts.View provideView(QrActivity qrActivity) {
        return qrActivity;
    }

    @Provides
    @PerActivity
    QrContracts.Presenter providePresenter(QRInterface qrInterface) {
        return new QrPresenter(qrInterface);
    }

    @Provides
    @PerActivity
    QRInterface providesQRInterface(BriteDatabase briteDatabase, D2 d2) {
        return new QRCodeGenerator(briteDatabase,d2);
    }
}
