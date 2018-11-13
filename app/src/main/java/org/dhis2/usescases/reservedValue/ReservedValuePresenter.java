package org.dhis2.usescases.reservedValue;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ReservedValuePresenter implements ReservedValueContracts.Presenter {

    private ReservedValueContracts.View view;
    private CompositeDisposable disposable;
    private  ReservedValueRepository repository;

    public ReservedValuePresenter(ReservedValueRepository repository) {
        this.repository = repository;
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
}
