package org.dhis2.usescases.datasets.datasetInitial;

import android.os.Bundle;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class DataSetInitialPresenter implements DataSetInitialContract.Presenter {

    private final SchedulerProvider schedulerProvider;
    private CompositeDisposable compositeDisposable;
    private DataSetInitialRepository dataSetInitialRepository;
    private DataSetInitialContract.View view;
    private String catCombo;
    private Integer openFuturePeriods = 0;
    private List<OrganisationUnit> orgUnits;

    public DataSetInitialPresenter(DataSetInitialRepository dataSetInitialRepository, SchedulerProvider schedulerProvider) {
        this.dataSetInitialRepository = dataSetInitialRepository;
        this.schedulerProvider = schedulerProvider;
    }


    @Override
    public void init(DataSetInitialContract.View view) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(
                dataSetInitialRepository.orgUnits()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {
                                    this.orgUnits = data;
                                    if(data.size() == 1)
                                        view.setOrgUnit(data.get(0));
                                },
                                Timber::d
                        )
        );

        compositeDisposable.add(
                dataSetInitialRepository.dataSet()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                dataSetInitialModel -> {
                                    catCombo = dataSetInitialModel.categoryCombo();
                                    openFuturePeriods = dataSetInitialModel.openFuturePeriods();
                                    view.setData(dataSetInitialModel);
                                },
                                Timber::d
                        ));
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onOrgUnitSelectorClick() {
        view.showOrgUnitDialog(orgUnits);

    }

    @Override
    public void onReportPeriodClick(PeriodType periodType) {
        compositeDisposable.add(
                dataSetInitialRepository.getDataInputPeriod()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(data -> {
                                    view.showPeriodSelector(periodType, data, openFuturePeriods);
                                },
                                Timber::e)
        );
    }

    @Override
    public void onCatOptionClick(String catOptionUid) {
        compositeDisposable.add(
                dataSetInitialRepository.catCombo(catOptionUid)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> view.showCatComboSelector(catOptionUid, data),
                                Timber::d
                        )
        );
    }

    @Override
    public void onActionButtonClick() {
        compositeDisposable.add(
                Flowable.zip(
                        dataSetInitialRepository.getCategoryOptionCombo(view.getSelectedCatOptions(), catCombo),
                        dataSetInitialRepository.getPeriodId(PeriodType.valueOf(view.getPeriodType()), view.getSelectedPeriod()),
                        Pair::create
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(response -> {
                                    Bundle bundle = DataSetTableActivity.getBundle(
                                            view.getDataSetUid(),
                                            view.getSelectedOrgUnit().uid(),
                                            view.getSelectedOrgUnit().name(),
                                            view.getPeriodType(),
                                            DateUtils.getInstance().getPeriodUIString(PeriodType.valueOf(view.getPeriodType()), view.getSelectedPeriod(), Locale.getDefault()),
                                            response.val1(),
                                            response.val0()
                                    );

                                    view.startActivity(DataSetTableActivity.class, bundle, true, false, null);
                                },
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
}
