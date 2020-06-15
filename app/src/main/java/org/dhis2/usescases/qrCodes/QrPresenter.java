package org.dhis2.usescases.qrCodes;

import android.annotation.SuppressLint;

import org.dhis2.data.qr.QRInterface;
import org.dhis2.data.schedulers.SchedulerProvider;

import androidx.annotation.NonNull;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class QrPresenter implements QrContracts.Presenter {

    private final QRInterface qrInterface;
    private final SchedulerProvider schedulerProvider;
    private QrContracts.View view;
    private CompositeDisposable disposable;

    QrPresenter(QRInterface qrInterface, SchedulerProvider schedulerProvider) {
        this.qrInterface = qrInterface;
        this.schedulerProvider = schedulerProvider;
        this.disposable = new CompositeDisposable();
    }

    @SuppressLint({"RxLeakedSubscription", "CheckResult"})
    public void generateQrs(@NonNull String teUid, @NonNull QrContracts.View view) {
        this.view = view;
        disposable.add(qrInterface.teiQRs(teUid)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view::showQR,
                        Timber::d
                )
        );

        /*disposable.add(qrInterface.getUncodedData(teUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::showQRBitmap,
                        Timber::e
                )
        );*/
    }

    @Override
    public void onBackClick() {
        if (view != null)
            view.onBackClick();
    }

    @Override
    public void onPrevQr() {
        view.onPrevQr();
    }

    @Override
    public void onNextQr() {
        view.onNextQr();
    }

    @Override
    public void onDetach() {
        disposable.clear();
    }

}
