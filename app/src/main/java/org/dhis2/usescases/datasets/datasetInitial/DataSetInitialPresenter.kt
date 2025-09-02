package org.dhis2.usescases.datasets.datasetInitial

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.dhis2.commons.extensions.inDateRange
import org.dhis2.commons.extensions.inOrgUnit
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import timber.log.Timber

class DataSetInitialPresenter(
    private val view: DataSetInitialContract.View,
    private val dataSetInitialRepository: DataSetInitialRepository,
    private val schedulerProvider: SchedulerProvider,
) : DataSetInitialContract.Presenter {
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var catCombo: String? = null
    private var openFuturePeriods: Int? = 0
    private var orgUnits: List<OrganisationUnit> =
        ArrayList()

    override fun init() {
        compositeDisposable.add(
            dataSetInitialRepository
                .orgUnits()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data: List<OrganisationUnit> ->
                        orgUnits = data
                        if (data.size == 1) view.setOrgUnit(data[0])
                    },
                    Timber::d,
                ),
        )
        compositeDisposable.add(
            dataSetInitialRepository
                .dataSet()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dataSetInitialModel: DataSetInitialModel ->
                        catCombo = dataSetInitialModel.categoryCombo
                        openFuturePeriods = dataSetInitialModel.openFuturePeriods
                        view.setData(dataSetInitialModel)
                    },
                    Timber::d,
                ),
        )
    }

    override fun onBackClick() {
        view.back()
    }

    override fun onOrgUnitSelectorClick() {
        view.showOrgUnitDialog(orgUnits)
    }

    override fun onReportPeriodClick(periodType: PeriodType) {
        view.showPeriodSelector(periodType, openFuturePeriods)
    }

    override fun onCatOptionClick(catOptionUid: String) {
        CoroutineTracker.increment()
        compositeDisposable.add(
            dataSetInitialRepository
                .catCombo(catOptionUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data ->
                        view.showCatComboSelector(
                            catOptionUid,
                            data.filter {
                                it.access().data().write() &&
                                    it.inDateRange(view.selectedPeriod) &&
                                    it.inOrgUnit(view.selectedOrgUnit?.uid())
                            },
                        )
                        CoroutineTracker.decrement()
                    },
                    Timber::d,
                ),
        )
    }

    override fun onActionButtonClick(periodType: PeriodType) {
        compositeDisposable.add(
            Flowable
                .zip(
                    dataSetInitialRepository.getCategoryOptionCombo(
                        view.selectedCatOptions,
                        catCombo,
                    ),
                    dataSetInitialRepository.getPeriodId(periodType, view.selectedPeriod),
                    BiFunction { val0: String?, val1: String? ->
                        Pair(
                            val0!!,
                            val1!!,
                        )
                    },
                ).subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { response: Pair<String, String> ->
                        view.navigateToDataSetTable(
                            response.first,
                            response.second,
                        )
                    },
                    Timber::d,
                ),
        )
    }

    override fun getCatOption(selectedOption: String): CategoryOption = dataSetInitialRepository.getCategoryOption(selectedOption)

    override fun onDettach() {
        compositeDisposable.clear()
    }

    override fun displayMessage(message: String) {
        view.displayMessage(message)
    }
}
