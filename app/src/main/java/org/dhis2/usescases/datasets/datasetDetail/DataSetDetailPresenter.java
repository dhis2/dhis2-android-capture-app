package org.dhis2.usescases.datasets.datasetDetail;

import android.os.Bundle;

import androidx.annotation.IntDef;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.granularsync.SyncStatusDialog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

public class DataSetDetailPresenter {

    private DataSetDetailView view;
    private DataSetDetailRepository dataSetDetailRepository;
    private SchedulerProvider schedulerProvider;
    private FilterManager filterManager;
    public CompositeDisposable compositeDisposable;
    private String dataSetUid;

    public DataSetDetailPresenter(DataSetDetailView view,
                                  DataSetDetailRepository dataSetDetailRepository,
                                  SchedulerProvider schedulerProvider,
                                  FilterManager filterManager) {
        this.view = view;
        this.dataSetDetailRepository = dataSetDetailRepository;
        this.schedulerProvider = schedulerProvider;
        this.filterManager = filterManager;
        compositeDisposable = new CompositeDisposable();
    }

    public void init(String dataSetUid) {

        this.dataSetUid = dataSetUid;
        getOrgUnits();

        compositeDisposable.add(
                filterManager.asFlowable()
                        .startWith(FilterManager.getInstance())
                        .flatMap(filterManager -> dataSetDetailRepository.dataSetGroups(
                                filterManager.getOrgUnitUidsFilters(),
                                filterManager.getPeriodFilters(),
                                filterManager.getStateFilters(),
                                filterManager.getCatOptComboFilters()))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setData,
                                Timber::d
                        )
        );

        compositeDisposable.add(
                filterManager.asFlowable()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                filterManager -> view.updateFilters(filterManager.getTotalFilters()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                filterManager.getPeriodRequest()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest),
                                Timber::e
                        ));

        compositeDisposable.add(
                dataSetDetailRepository.catOptionCombos()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setCatOptionComboFilter,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                dataSetDetailRepository.canWriteAny()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setWritePermission,
                                Timber::e
                        ));
    }

    public void addDataSet() {
        view.startNewDataSet();
    }

    public void onBackClick() {
        if (view != null)
            view.back();
    }

    public void onDataSetClick(String orgUnit, String orgUnitName, String periodId, String periodType, String initPeriodType, String catOptionComb) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ORG_UNIT, orgUnit);
        bundle.putString(Constants.ORG_UNIT_NAME, orgUnitName);
        bundle.putString(Constants.PERIOD_TYPE_DATE, initPeriodType);
        bundle.putString(Constants.PERIOD_TYPE, periodType);
        bundle.putString(Constants.PERIOD_ID, periodId);
        bundle.putString(Constants.CAT_COMB, catOptionComb);
        bundle.putString(Constants.DATA_SET_UID, view.dataSetUid());
        bundle.putBoolean(Constants.ACCESS_DATA, view.accessDataWrite());
        bundle.putString(Constants.PERIOD_ID, periodId);
        view.startActivity(DataSetTableActivity.class, bundle, false, false, null);
    }

    public void showFilter() {
        view.showHideFilter();
    }

    public void getOrgUnits() {
        compositeDisposable.add(
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
        compositeDisposable.clear();
    }

    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    public void onSyncIconClick(String orgUnit, String attributeCombo, String periodId) {
        view.showSyncDialog(
                new SyncStatusDialog.Builder()
                        .setConflictType(SyncStatusDialog.ConflictType.DATA_VALUES)
                        .setUid(dataSetUid)
                        .setOrgUnit(orgUnit)
                        .setAttributeOptionCombo(attributeCombo)
                        .setPeriodId(periodId)
                        .onDismissListener(hasChanged -> {
                            if(hasChanged)
                                filterManager.publishData();
                        })
                        .build()
        );
    }

    public void clearFilterClick() {
        filterManager.clearAllFilters();
        view.clearFilters();
    }
}
