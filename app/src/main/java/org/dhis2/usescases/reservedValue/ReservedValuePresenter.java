package org.dhis2.usescases.reservedValue;

import org.hisp.dhis.android.core.D2;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
                        () -> {
                            d2.syncTrackedEntityAttributeReservedValues(reservedValue.uid(), reservedValue.orgUnitUid(), 100 - reservedValue.reservedValues());
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
        if (view != null)
            view.onBackClick();
    }
}
