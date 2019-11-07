package org.dhis2.usescases.reservedValue;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.maintenance.D2Error;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ReservedValuePresenter {

    private final SchedulerProvider schedulerProvider;
    private ReservedValueContracts.View view;
    CompositeDisposable disposable;
    private ReservedValueRepository repository;
    private D2 d2;
    private FlowableProcessor<Boolean> updateProcessor;

    public ReservedValuePresenter(ReservedValueRepository repository, D2 d2, SchedulerProvider schedulerProvider, ReservedValueContracts.View view) {
        this.repository = repository;
        this.d2 = d2;
        this.updateProcessor = PublishProcessor.create();
        this.schedulerProvider = schedulerProvider;
        this.view = view;
        this.disposable = new CompositeDisposable();
    }

    public void init() {
        disposable.add(
                updateProcessor
                        .startWith(true)
                        .flatMap(update -> repository.getDataElements())
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setDataElements,
                                Timber::e
                        )
        );
    }

    public void onClickRefill(ReservedValueModel reservedValue) {
        disposable.add(
                d2.trackedEntityModule()
                        .reservedValueManager()
                        .downloadReservedValues(reservedValue.uid(), 100)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.io())
                        .subscribe(
                                d2Progress -> Timber.d("Rserved value manager: %s", d2Progress.percentage()),
                                this::onReservedValuesError,
                                () -> updateProcessor.onNext(true))
        );
    }

    private void onReservedValuesError(Throwable e) {
        if (e instanceof D2Error) {
            view.showReservedValuesError();
        } else {
            Timber.e(e);
        }
    }

    public void onBackClick() {
        if (view != null)
            view.onBackClick();
    }

    public void onPause() {
        disposable.clear();
    }

    public CompositeDisposable getDisposable() {
        return disposable;
    }
}
