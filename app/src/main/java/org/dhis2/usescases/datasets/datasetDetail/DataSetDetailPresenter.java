package org.dhis2.usescases.datasets.datasetDetail;

import androidx.annotation.VisibleForTesting;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.filters.FilterManager;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class DataSetDetailPresenter {

    private DataSetDetailView view;
    private DataSetDetailRepository dataSetDetailRepository;
    private SchedulerProvider schedulerProvider;
    private FilterManager filterManager;

    CompositeDisposable disposable;

    public DataSetDetailPresenter(DataSetDetailView view,
                                  DataSetDetailRepository dataSetDetailRepository,
                                  SchedulerProvider schedulerProvider,
                                  FilterManager filterManager) {

        this.view = view;
        this.dataSetDetailRepository = dataSetDetailRepository;
        this.schedulerProvider = schedulerProvider;
        this.filterManager = filterManager;
        disposable = new CompositeDisposable();
    }

    public void init() {
        getOrgUnits();

        disposable.add(
                filterManager.asFlowable()
                        .startWith(filterManager)
                        .flatMap(filterManager -> dataSetDetailRepository.dataSetGroups(
                                filterManager.getOrgUnitUidsFilters(),
                                filterManager.getPeriodFilters(),
                                filterManager.getStateFilters(),
                                filterManager.getCatOptComboFilters()))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(data -> {
                                    view.setData(data);
                                    view.updateFilters(filterManager.getTotalFilters());
                                },
                                Timber::d
                        )
        );

        disposable.add(
                filterManager.getPeriodRequest()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest),
                                Timber::e
                        ));

        disposable.add(
                dataSetDetailRepository.catOptionCombos()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                catCombos -> view.setCatOptionComboFilter(catCombos),
                                Timber::e
                        )
        );

        disposable.add(
                dataSetDetailRepository.canWriteAny()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                canWrite -> view.setWritePermission(canWrite),
                                Timber::e
                        ));
    }

    public void addDataSet() {
        view.startNewDataSet();
    }

    public void onBackClick() {
        view.back();
    }

    public void openDataSet(DataSetDetailModel dataSet) {
        view.openDataSet(dataSet);
    }

    public void showFilter() {
        view.showHideFilter();
    }

    @VisibleForTesting
    public void getOrgUnits() {
        disposable.add(
                filterManager.ouTreeFlowable()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );
    }


    public void onDettach() {
        disposable.clear();
    }

    public void displayMessage(String message) {
        view.displayMessage(message);
    }


    public void onSyncIconClick(DataSetDetailModel dataSet) {
        view.showSyncDialog(dataSet);
    }

    public void updateFilters() {
        filterManager.publishData();
    }

    public void clearFilterClick() {
        filterManager.clearAllFilters();
        view.clearFilters();
    }
}
