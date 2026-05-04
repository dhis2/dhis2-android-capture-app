package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.idling.AnalyticsCountingIdlingResource
import dhis2.org.analytics.charts.ui.ChartFilter
import dhis2.org.analytics.charts.ui.ChartModel
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber

class IndicatorsPresenter(
    val dispatcherProvider: DispatcherProvider,
    val view: IndicatorsView,
    val indicatorRepository: IndicatorRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.io())

    fun init() {
        AnalyticsCountingIdlingResource.increment()
        fetchData()
    }

    private fun fetchData() {
        scope.launch {
            try {
                val data = indicatorRepository.fetchData()
                withContext(dispatcherProvider.ui()) {
                    view.swapAnalytics(data)
                }
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    fun onDettach() = scope.cancel()

    fun displayMessage(message: String) = view.displayMessage(message)

    fun filterByPeriod(
        chartModel: ChartModel,
        selectedPeriods: List<RelativePeriod>,
        lineListingColumnId: Int?,
    ) {
        indicatorRepository.filterByPeriod(chartModel, selectedPeriods, lineListingColumnId)
        fetchData()
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
        fetchData()
    }

    fun resetFilter(
        chartModel: ChartModel,
        filterType: ChartFilter,
    ) {
        chartModel.graph.visualizationUid?.let { _ ->
            when (filterType) {
                ChartFilter.PERIOD ->
                    indicatorRepository.filterByPeriod(
                        chartModel,
                        emptyList(),
                        null,
                    )

                ChartFilter.ORG_UNIT ->
                    indicatorRepository.filterByOrgUnit(
                        chartModel,
                        emptyList(),
                        OrgUnitFilterType.NONE,
                        null,
                    )

                ChartFilter.COLUMN ->
                    indicatorRepository.filterLineListing(
                        chartModel,
                        null,
                    )
            }
        }
        fetchData()
    }
}
