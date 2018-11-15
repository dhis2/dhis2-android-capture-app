package org.dhis2.usescases.reservedValue;

import android.annotation.SuppressLint;

import org.hisp.dhis.android.core.D2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.completable.CompletableEmpty;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ReservedValuePresenter implements ReservedValueContracts.Presenter {

    private ReservedValueContracts.View view;
    private CompositeDisposable disposable;
    private ReservedValueRepository repository;
    private D2 d2;

    public ReservedValuePresenter(ReservedValueRepository repository, D2 d2) {
        this.repository = repository;
        this.d2 = d2;
    }

    @Override
    public void init(ReservedValueContracts.View view) {
        this.view = view;
        disposable = new CompositeDisposable();

        disposable.add(
                repository.getDataElements()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setDataElements,
                                Timber::e
                        )
        );
    }

    @Override
    public void onClickRefill(ReservedValueModel reservedValue) {

        disposable.add(Completable.complete()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {d2.syncTrackedEntityAttributeReservedValue(reservedValue.uid(), reservedValue.orgUnitUid());
                            disposable.add(repository.getDataElements()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            view::setDataElements,
                                            Timber::e
                                    ));
                        },
                        Timber::e));


    }

    @Override
    public void onBackClick() {
        if(view != null)
            view.onBackClick();
    }
}
