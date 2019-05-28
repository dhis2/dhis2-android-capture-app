package org.dhis2.usescases.reservedValue;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.maintenance.D2Error;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ReservedValuePresenter implements ReservedValueContracts.Presenter {

    private ReservedValueContracts.View view;
    private CompositeDisposable disposable;
    private ReservedValueRepository repository;
    private D2 d2;
    private FlowableProcessor<Boolean> updateProcessor;

    public ReservedValuePresenter(ReservedValueRepository repository, D2 d2) {
        this.repository = repository;
        this.d2 = d2;
        this.updateProcessor = PublishProcessor.create();
    }

    @Override
    public void init(ReservedValueContracts.View view) {
        this.view = view;
        disposable = new CompositeDisposable();

        disposable.add(
                updateProcessor
                        .startWith(true)
                        .flatMap(update -> repository.getDataElements())
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
        disposable.add(
                Completable.fromAction(() ->
                        d2.trackedEntityModule()
                                .reservedValueManager
                                .syncReservedValues(reservedValue.uid(), reservedValue.orgUnitUid(), 100)
                ).subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                () -> updateProcessor.onNext(true),
                                this::onReservedValuesError)
        );
    }

    private void onReservedValuesError(Throwable e) {
        if (e instanceof D2Error) {
            view.showReservedValuesError();
        } else {
            Timber.e(e);
        }
    }

    @Override
    public void onBackClick() {
        if (view != null)
            view.onBackClick();
    }

    @Override
    public void onPause() {
        disposable.clear();
    }
}
