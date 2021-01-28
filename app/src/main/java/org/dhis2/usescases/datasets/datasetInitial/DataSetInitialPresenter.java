package org.dhis2.usescases.datasets.datasetInitial;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class DataSetInitialPresenter implements DataSetInitialContract.Presenter {

    private final SchedulerProvider schedulerProvider;
    public CompositeDisposable compositeDisposable;
    private DataSetInitialRepository dataSetInitialRepository;
    private DataSetInitialContract.View view;
    private String catCombo;
    private Integer openFuturePeriods = 0;
    private List<OrganisationUnit> orgUnits = new ArrayList<>();

    public DataSetInitialPresenter(DataSetInitialContract.View view, DataSetInitialRepository dataSetInitialRepository, SchedulerProvider schedulerProvider) {
        this.view = view;
        this.dataSetInitialRepository = dataSetInitialRepository;
        this.schedulerProvider = schedulerProvider;
        compositeDisposable = new CompositeDisposable();
    }


    @Override
    public void init() {

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
    public void onActionButtonClick(PeriodType periodType) {
        compositeDisposable.add(
                Flowable.zip(
                        dataSetInitialRepository.getCategoryOptionCombo(view.getSelectedCatOptions(), catCombo),
                        dataSetInitialRepository.getPeriodId(periodType, view.getSelectedPeriod()),
                        Pair::create
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(response -> view.navigateToDataSetTable(response.val0(), response.val1()),
                                Timber::e
                        )
        );

    }

    @Override
    public CategoryOption getCatOption(String selectedOption) {
        return dataSetInitialRepository.getCategoryOption(selectedOption);
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
