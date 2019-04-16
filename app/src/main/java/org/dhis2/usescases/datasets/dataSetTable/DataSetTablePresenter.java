package org.dhis2.usescases.datasets.dataSetTable;

import android.os.Bundle;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepository;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetTablePresenter implements DataSetTableContract.Presenter {

    private final DataSetTableRepository tableRepository;
    private final DataSetInitialRepository initialRepository;
    DataSetTableContract.View view;
    private CompositeDisposable compositeDisposable;

    private String orgUnitUid;
    private String periodTypeName;
    private String periodFinalDate;
    private String catCombo;
    private boolean open = true;
    private String periodId;

    public DataSetTablePresenter(DataSetTableRepository dataSetTableRepository,
                                 DataSetInitialRepository dataSetInitialRepository) {
        this.tableRepository = dataSetTableRepository;
        this.initialRepository = dataSetInitialRepository;
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void init(DataSetTableContract.View view, String orgUnitUid, String periodTypeName, String catCombo,
                     String periodFinalDate, String periodId) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
        this.orgUnitUid = orgUnitUid;
        this.periodTypeName = periodTypeName;
        this.periodFinalDate = periodFinalDate;
        this.catCombo = catCombo;
        this.periodId = periodId;

        compositeDisposable.add(
                Flowable.zip(
                        tableRepository.getDataElements(),
                        tableRepository.getCatOptions(),
                        Pair::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    view.setDataElements(data.val0(), data.val1());
                                },
                                Timber::e
                        )
        );
        compositeDisposable.add(
                initialRepository.dataSet()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::renderDetails,
                                Timber::d
                        ));

    }

    @Override
    public void onDettach() {
        compositeDisposable.dispose();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    public String getOrgUnitUid() {
        return orgUnitUid;
    }

    public String getPeriodTypeName() {
        return periodTypeName;
    }

    public String getPeriodFinalDate() {
        return periodFinalDate;
    }

    public String getPeriodId(){
        return periodId;
    }

    public String getCatCombo() {
        return catCombo;
    }

    @Override
    public void optionsClick() {
        view.showOptions(open);
        open =!open;

        if(!open) {
            compositeDisposable.add(
                    initialRepository.dataSet()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    view::setData,
                                    Timber::d
                            ));
        }
    }

    @Override
    public void onOrgUnitSelectorClick() {
        compositeDisposable.add(
                initialRepository.orgUnits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.showOrgUnitDialog(data),
                                Timber::d
                        )
        );
    }

    @Override
    public void onReportPeriodClick() {

        //Aqui meter los data input
        view.showPeriodSelector(PeriodType.valueOf(periodTypeName));

    }

    @Override
    public void onRefreshClick() {
        if(view.getSelectedOrgUnit()!=null || view.getSelectedPeriod()!=null) {
            Bundle bundle = DataSetTableActivity.getBundle(
                    view.getDataSetUid(),
                    view.getSelectedOrgUnit() != null ? view.getSelectedOrgUnit().uid() : orgUnitUid,
                    view.getSelectedOrgUnit() != null ? view.getSelectedOrgUnit().name() : view.getOrgUnitName(),
                    periodTypeName,
                    view.getSelectedPeriod() != null ? DateUtils.getInstance().getPeriodUIString(PeriodType.valueOf(periodTypeName), view.getSelectedPeriod(), Locale.getDefault()) : periodFinalDate,
                    view.getSelectedPeriod() != null ? DateUtils.getInstance().generateId(PeriodType.valueOf(periodTypeName), view.getSelectedPeriod(), Locale.getDefault()) : periodId,
                    catCombo/*view.getSelectedCatOptions() Fixed is the same always*/
            );
            view.startActivity(DataSetTableActivity.class, bundle, true, false, null);
        }
        else{
            view.showOptions(open);
        }
    }

    @Override
    public void onCatOptionClick(String catOptionUid) {
        compositeDisposable.add(
                initialRepository.catCombo(catOptionUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.showCatComboSelector(catOptionUid, data),
                                Timber::d
                        )
        );
    }

    @Override
    public void onClickSelectTable(int numTable) {
        view.goToTable(numTable);
    }

    @Override
    public void setCurrentNumTables(int numTables) {
        view.setCurrentNumTables(numTables);
    }
}
