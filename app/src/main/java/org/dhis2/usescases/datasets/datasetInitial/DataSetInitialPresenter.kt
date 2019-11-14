package org.dhis2.usescases.datasets.datasetInitial

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import timber.log.Timber

class DataSetInitialPresenter(
    private val view: DataSetInitialView,
    private val dataSetInitialRepository: DataSetInitialRepository,
    private val schedulerProvider: SchedulerProvider
) {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var catCombo: String? = null
    private var openFuturePeriods: Int? = 0
    private var orgUnits: List<OrganisationUnit> = arrayListOf()

    fun init() {
        compositeDisposable.add(
            dataSetInitialRepository.orgUnits()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { orgUnits ->
                        this.orgUnits = orgUnits
                        if (orgUnits.size == 1) {
                            view.setOrgUnit(orgUnits[0])
                        }
                    },
                    Timber::e
                )
        )

        compositeDisposable.add(
            dataSetInitialRepository.dataSet()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dataSetInitialModel ->
                        catCombo = dataSetInitialModel.categoryCombo()
                        openFuturePeriods = dataSetInitialModel.openFuturePeriods()
                        view.setData(dataSetInitialModel)
                    },
                    Timber::e
                )
        )
    }

    fun onBackClick() {
        view.back()
    }

    fun onOrgUnitSelectorClick() {
        view.showOrgUnitDialog(orgUnits)
    }

    fun onReportPeriodClick(periodType: PeriodType) {
        compositeDisposable.add(
            dataSetInitialRepository.dataInputPeriod
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { periods ->
                        view.showPeriodSelector(
                            periodType,
                            periods,
                            openFuturePeriods
                        )
                    },
                    Timber::e
                )
        )
    }

    fun onCatOptionClick(catOptionUid: String) {
        compositeDisposable.add(
            dataSetInitialRepository.catCombo(catOptionUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { catOptions -> view.showCatComboSelector(catOptionUid, catOptions) },
                    Timber::e
                )
        )
    }

    fun onActionButtonClick(periodType: PeriodType) {
        compositeDisposable.add(
            Flowable.zip<String, String, Pair<String, String>>(
                dataSetInitialRepository.getCategoryOptionCombo(
                    view.getSelectedCatOptions(),
                    catCombo
                ),
                dataSetInitialRepository.getPeriodId(
                    periodType,
                    view.selectedPeriod
                ),
                BiFunction<String, String, Pair<String, String>> { catOptionCombo, periodId ->
                    catOptionCombo to periodId
                }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { (catOptionCombo, periodId) ->
                        view.navigateToDataSetTable(catOptionCombo, periodId)
                    },
                    Timber::e
                )
        )
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }
}
