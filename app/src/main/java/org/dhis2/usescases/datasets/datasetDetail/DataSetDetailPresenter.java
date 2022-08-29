package org.dhis2.usescases.datasets.datasetDetail;

import static org.dhis2.commons.matomo.Actions.GRANULAR_SYNC;
import static org.dhis2.commons.matomo.Actions.OPEN_ANALYTICS;
import static org.dhis2.commons.matomo.Categories.DATASET_LIST;
import static org.dhis2.commons.matomo.Labels.CLICK;

import androidx.annotation.VisibleForTesting;

import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class DataSetDetailPresenter {

    private DataSetDetailView view;
    private DataSetDetailRepository dataSetDetailRepository;
    private SchedulerProvider schedulerProvider;
    private FilterManager filterManager;
    private FilterRepository filterRepository;
    private DisableHomeFiltersFromSettingsApp disableHomFilters;
    private MatomoAnalyticsController matomoAnalyticsController;

    CompositeDisposable disposable;

    public DataSetDetailPresenter(DataSetDetailView view,
                                  DataSetDetailRepository dataSetDetailRepository,
                                  SchedulerProvider schedulerProvider,
                                  FilterManager filterManager,
                                  FilterRepository filterRepository,
                                  DisableHomeFiltersFromSettingsApp disableHomFilters,
                                  MatomoAnalyticsController matomoAnalyticsController
    ) {

        this.view = view;
        this.dataSetDetailRepository = dataSetDetailRepository;
        this.schedulerProvider = schedulerProvider;
        this.filterManager = filterManager;
        this.filterRepository = filterRepository;
        this.disableHomFilters = disableHomFilters;
        this.matomoAnalyticsController = matomoAnalyticsController;
        disposable = new CompositeDisposable();
    }

    public void init() {
        getOrgUnits();

        disposable.add(
                filterManager.asFlowable().startWith(filterManager)
                        .flatMap(filterManager -> Flowable.just(filterRepository.dataSetFilters(dataSetDetailRepository.getDataSetUid())))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(filterItems -> {
                                    if (filterItems.isEmpty()) {
                                        view.hideFilters();
                                    } else {
                                        view.setFilters(filterItems);
                                    }
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
                                periodRequest -> view.showPeriodRequest(periodRequest.getFirst()),
                                Timber::e
                        ));


        disposable.add(FilterManager.getInstance().getCatComboRequest()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        catComboUid -> view.showCatOptComboDialog(catComboUid),
                        Timber::e
                )
        );
    }

    public void onSyncClicked(){
        view.showGranularSync();
    }

    public void onBackClick() {
        view.back();
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

    public void clearFilterClick() {
        filterManager.clearAllFilters();
        view.clearFilters();
    }

    public void filterCatOptCombo(String selectedCatOptionCombo) {
        FilterManager.getInstance().addCatOptCombo(
                dataSetDetailRepository.getCatOptCombo(selectedCatOptionCombo)
        );
    }

    public void clearFilterIfDatasetConfig() {
        List<FilterItem> filters = filterRepository.homeFilters();
        disableHomFilters.execute(filters);
    }

    public void setOpeningFilterToNone() {
        filterRepository.collapseAllFilters();
    }

    public void setOrgUnitFilters(List<OrganisationUnit> selectedOrgUnits) {
        FilterManager.getInstance().addOrgUnits(selectedOrgUnits);
    }

    public void refreshList() {
        filterManager.publishData();
    }

    public void trackDataSetAnalytics() {
        matomoAnalyticsController.trackEvent(DATASET_LIST, OPEN_ANALYTICS, CLICK);
    }

    public void trackDataSetGranularSync() {
        matomoAnalyticsController.trackEvent(DATASET_LIST,GRANULAR_SYNC, CLICK );
    }
}
