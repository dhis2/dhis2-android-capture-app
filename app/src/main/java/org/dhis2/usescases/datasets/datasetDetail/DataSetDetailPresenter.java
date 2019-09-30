package org.dhis2.usescases.datasets.datasetDetail;

import android.os.Bundle;

import androidx.annotation.IntDef;

import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.main.program.SyncStatusDialog;
import org.dhis2.utils.Constants;
import org.dhis2.utils.filters.FilterManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetDetailPresenter implements DataSetDetailContract.Presenter {

    private DataSetDetailRepository dataSetDetailRepository;
    private DataSetDetailContract.View view;
    private CompositeDisposable compositeDisposable;
    private Map<String, String> mapPeriodAvailable;
    private FlowableProcessor<Boolean> processor;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LastSearchType.DATES, LastSearchType.DATE_RANGES})
    public @interface LastSearchType {
        int DATES = 1;
        int DATE_RANGES = 32;
    }

    public DataSetDetailPresenter(DataSetDetailRepository dataSetDetailRepository) {
        this.dataSetDetailRepository = dataSetDetailRepository;
        compositeDisposable = new CompositeDisposable();
        mapPeriodAvailable = new HashMap<>();
    }

    @Override
    public void init(DataSetDetailContract.View view) {
        this.view = view;
        this.processor = PublishProcessor.create();
        getOrgUnits();
        setDataSet(true);
        manageProcessorDismissDialog();

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                filterManager -> view.updateFilters(filterManager.getTotalFilters()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().getPeriodRequest()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                periodRequest -> view.showPeriodRequest(periodRequest),
                                Timber::e
                        ));

        compositeDisposable.add(
                dataSetDetailRepository.catOptionCombos()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::setCatOptionComboFilter,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                dataSetDetailRepository.canWriteAny()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setWritePermission,
                                Timber::e
                        ));
    }

    private void manageProcessorDismissDialog(){
        compositeDisposable.add(processor
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bool -> setDataSet(true), Timber::d));
    }

    private void setDataSet(boolean isInit){
        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .startWith(FilterManager.getInstance())
                        .flatMap(filterManager -> dataSetDetailRepository.dataSetGroups(
                                filterManager.getOrgUnitUidsFilters(),
                                filterManager.getPeriodFilters(),
                                filterManager.getStateFilters(),
                                filterManager.getCatOptComboFilters()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                dataSetDetailModels -> {
                                    if(isInit)
                                        for(DataSetDetailModel dataset: dataSetDetailModels)
                                            mapPeriodAvailable.put(dataset.periodId(), dataset.namePeriod());

                                    view.setData(dataSetDetailModels);
                                    //view.setWritePermission(view.accessDataWrite());
                                },
                                Timber::d
                        )
        );
    }


    @Override
    public void addDataSet() {
        view.startNewDataSet();
    }

    @Override
    public void onBackClick() {
        if (view != null)
            view.back();
    }

    @Override
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

    @Override
    public void showFilter() {
        view.showHideFilter();
    }

    public void getOrgUnits() {
        compositeDisposable.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );
    }


    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }


    @Override
    public void onSyncIconClick(String orgUnit, String attributeCombo, String periodId) {
        view.showSyncDialog(orgUnit, attributeCombo, periodId, SyncStatusDialog.ConflictType.DATA_VALUES, processor);
    }

    @Override
    public void clearFilterClick() {
        FilterManager.getInstance().clearAllFilters();
        view.clearFilters();
    }
}
