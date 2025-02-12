package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.ChartFilter
import dhis2.org.analytics.charts.ui.ChartModel
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber

class IndicatorsPresenter(
    val schedulerProvider: SchedulerProvider,
    val view: IndicatorsView,
    val indicatorRepository: IndicatorRepository,
) {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    val publishProcessor = PublishProcessor.create<Unit>()

    fun init() {
        compositeDisposable.add(
            publishProcessor.startWith(Unit)
                .flatMap { indicatorRepository.fetchData() }
                .defaultSubscribe(schedulerProvider, { view.swapAnalytics(it) }, { Timber.d(it) }),
        )
    }

    fun onDettach() = compositeDisposable.clear()

    fun displayMessage(message: String) = view.displayMessage(message)
    fun filterByPeriod(
        chartModel: ChartModel,
        selectedPeriods: List<RelativePeriod>,
        lineListingColumnId: Int?,
    ) {
        indicatorRepository.filterByPeriod(chartModel, selectedPeriods, lineListingColumnId)
        publishProcessor.onNext(Unit)
    }

    fun filterByOrgUnit(
        chartModel: ChartModel,
        selectedPeriods: List<OrganisationUnit>,
        filterType: OrgUnitFilterType,
        lineListingColumnId: Int?,
    ) {
        indicatorRepository.filterByOrgUnit(
            chartModel,
            selectedPeriods,
            filterType,
            lineListingColumnId,
        )
        publishProcessor.onNext(Unit)
    }

    fun resetFilter(chartModel: ChartModel, filterType: ChartFilter) {
        chartModel.graph.visualizationUid?.let { _ ->
            when (filterType) {
                ChartFilter.PERIOD -> indicatorRepository.filterByPeriod(
                    chartModel,
                    emptyList(),
                    null,
                )

                ChartFilter.ORG_UNIT -> indicatorRepository.filterByOrgUnit(
                    chartModel,
                    emptyList(),
                    OrgUnitFilterType.NONE,
                    null,
                )

                ChartFilter.COLUMN -> indicatorRepository.filterLineListing(
                    chartModel,
                    null,
                )
            }
        }
        publishProcessor.onNext(Unit)
    }
}
