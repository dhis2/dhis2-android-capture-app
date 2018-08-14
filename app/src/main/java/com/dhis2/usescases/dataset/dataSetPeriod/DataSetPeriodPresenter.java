package com.dhis2.usescases.dataset.dataSetPeriod;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by frodriguez on 7/20/2018.
 */
public class DataSetPeriodPresenter implements DataSetPeriodContract.Presenter {

    private DataSetPeriodRepository repository;
    private DataSetPeriodContract.View view;
    private CompositeDisposable disposable;

    private String dataSetId;

    DataSetPeriodPresenter(DataSetPeriodRepository repository){
        this.repository = repository;
    }

    @Override
    public void init(DataSetPeriodContract.View view, String dataSetId) {
        this.view = view;
        this.dataSetId = dataSetId;

        disposable.add(
                repository.getDataSet(dataSetId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::setDataSet, Timber::d)
        );
    }

    @Override
    public void onBackClick() {
        view.displayMessage("back back");
        view.back();
    }

    @Override
    public void showFilter() {
    }

    @Override
    public void addDataSet() {
    }
}
