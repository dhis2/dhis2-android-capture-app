package org.dhis2.usescases.datasets.datasetInitial

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import timber.log.Timber

class DataSetInitialPresenter(
    private val view: DataSetInitialContract.View,
    private val dataSetInitialRepository: DataSetInitialRepository,
    private val schedulerProvider: SchedulerProvider
) {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var catCombo: String? = null
    private var openFuturePeriods: Int? = 0
    private var orgUnits: List<OrganisationUnit>? = null

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
                    { Timber.d(it) }
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
                    { Timber.d(it) }
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
                    { Timber.e(it) }
                )
        )
    }

    fun onCatOptionClick(catOptionUid: String) {
        compositeDisposable.add(
            dataSetInitialRepository.catCombo(catOptionUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data -> view.showCatComboSelector(catOptionUid, data) }, { Timber.d(it) }
                )
        )
    }

    fun onActionButtonClick() {
        compositeDisposable.add(
            Flowable.zip<String, String, Pair<String, String>>(
                dataSetInitialRepository.getCategoryOptionCombo(
                    view.selectedCatOptions,
                    catCombo
                ),
                dataSetInitialRepository.getPeriodId(
                    PeriodType.valueOf(view.periodType),
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
                    { Timber.e(it) }
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
