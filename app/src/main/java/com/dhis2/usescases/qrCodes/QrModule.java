package com.dhis2.usescases.qrCodes;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.qr.QRInterface;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 30/11/2017.
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
