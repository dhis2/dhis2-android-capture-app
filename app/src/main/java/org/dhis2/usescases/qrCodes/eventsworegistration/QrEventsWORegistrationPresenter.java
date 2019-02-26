package org.dhis2.usescases.qrCodes.eventsworegistration;

import android.annotation.SuppressLint;

import org.dhis2.data.qr.QRInterface;

import androidx.annotation.NonNull;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class QrEventsWORegistrationPresenter extends QrGlobalPresenter implements QrEventsWORegistrationContracts.Presenter {

    private final QRInterface qrInterface;

    QrEventsWORegistrationPresenter(QRInterface qrInterface) {
        this.qrInterface = qrInterface;
    }

    @SuppressLint({"RxLeakedSubscription", "CheckResult"})
    public void generateQrs(@NonNull String eventUid, @NonNull QrEventsWORegistrationContracts.View view) {
        this.qrEventWORegistrationView = view;
        qrInterface.eventWORegistrationQRs(eventUid)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::showQR,
                        Timber::d
                );
    }
}
