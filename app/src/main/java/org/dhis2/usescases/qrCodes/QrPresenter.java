package org.dhis2.usescases.qrCodes;

import android.annotation.SuppressLint;

import org.dhis2.data.qr.QRInterface;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrGlobalPresenter;

import androidx.annotation.NonNull;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class QrPresenter extends QrGlobalPresenter implements QrContracts.Presenter {

    private final QRInterface qrInterface;

    QrPresenter(QRInterface qrInterface) {
        this.qrInterface = qrInterface;
    }

    @SuppressLint({"RxLeakedSubscription", "CheckResult"})
    public void generateQrs(@NonNull String teUid, @NonNull QrContracts.View view) {
        this.qrView = view;
        qrInterface.teiQRs(teUid)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::showQR,
                        Timber::d
                );
    }
}
