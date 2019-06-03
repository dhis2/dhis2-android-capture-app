package org.dhis2.usescases.datasets.datasetDetail;

import android.os.Bundle;

import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialActivity;
import org.dhis2.usescases.main.program.SyncStatusDialog;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrgUnitUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.IntDef;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetDetailPresenter implements DataSetDetailContract.Presenter {

    private DataSetDetailRepository dataSetDetailRepository;
    private DataSetDetailContract.View view;
    private CompositeDisposable compositeDisposable;
    private List<OrganisationUnit> orgUnits;
    private List<String> selectedOrgUnits;
    private List<String> selectedPeriods = new ArrayList<>();
    private Map<String, String> mapPeriodAvailable;

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
        getOrgUnits(null);
        setDataSet(true);
    }

    private void setDataSet(boolean isInit){
        compositeDisposable.add(
                view.dataSetPage()
                        .startWith(0)
                        .flatMap(page -> dataSetDetailRepository.dataSetGroups(view.dataSetUid(), selectedOrgUnits, selectedPeriods, page))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                dataSetDetailModels -> {
                                    if(isInit)
                                        for(DataSetDetailModel dataset: dataSetDetailModels)
                                            mapPeriodAvailable.put(dataset.periodId(), dataset.namePeriod());

                                    view.setData(dataSetDetailModels);
                                    view.setWritePermission(view.accessDataWrite());
                                },
                                Timber::d
                        )
        );
    }

    @Override
    public void onDateRangeButtonClick() {
        view.showRageDatePicker();
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
    }


    @Override
    public void addDataSet() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, view.dataSetUid());

        view.startActivity(DataSetInitialActivity.class,bundle,false,false,null);
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
    public List<OrganisationUnit> getOrgUnits() {
        return orgUnits;
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }

    @Override
    public void getOrgUnits(Date date) {
        compositeDisposable.add(dataSetDetailRepository.orgUnits()
                .map(orgUnits -> {
                    this.orgUnits = orgUnits;
                    return OrgUnitUtils.renderTree_2(view.getContext(), orgUnits, true);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        treeNode -> view.addTree(treeNode),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }


    @Override
    public void getDataSetWithDates(List<String> selected, List<String> orgUnitQuery) {
        this.selectedOrgUnits = orgUnitQuery;
        selectedPeriods.clear();
        for(Map.Entry<String, String> entry : mapPeriodAvailable.entrySet())
            if(selected.contains(entry.getValue()) && !selectedPeriods.contains(entry.getValue()))
                selectedPeriods.add(entry.getKey());

        setDataSet(false);
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
    public Map<String, String> getPeriodAvailableForFilter() {
        return mapPeriodAvailable;
    }

    @Override
    public String getFirstPeriodSelected(){
        for(Map.Entry<String, String> entry : mapPeriodAvailable.entrySet())
            if(!selectedPeriods.isEmpty() && selectedPeriods.get(0).equals(entry.getKey()))
                return entry.getValue();
        return "";
    }

    @Override
    public void onSyncIconClick(String orgUnit, String attributeCombo, String periodId) {
        view.showSyncDialog(orgUnit, attributeCombo, periodId, SyncStatusDialog.ConflictType.DATA_VALUES);
    }
}
