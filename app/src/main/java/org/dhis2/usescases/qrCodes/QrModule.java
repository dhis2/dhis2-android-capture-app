package org.dhis2.usescases.qrCodes;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.qr.QRInterface;

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
}
