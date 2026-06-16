package dhis2.org.analytics.charts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.data.AnalyticGroup
import dhis2.org.analytics.charts.domain.GetEnrollmentAnalyticsUseCase
import dhis2.org.analytics.charts.idling.AnalyticsCountingIdlingResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber

const val MIN_SIZE_TO_SHOW = 2

class GroupAnalyticsViewModel(
    private val mode: AnalyticMode,
    private val uid: String?,
    private val charts: Charts,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val getEnrollmentAnalyticsUseCase: GetEnrollmentAnalyticsUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {
    private val _chipItems = MutableStateFlow<Result<List<AnalyticGroup>>?>(null)
    val chipItems: StateFlow<Result<List<AnalyticGroup>>?> = _chipItems.asStateFlow()
    private val _analytics = MutableStateFlow<Result<List<AnalyticsModel>>?>(null)
    val analytics: StateFlow<Result<List<AnalyticsModel>>?> = _analytics.asStateFlow()
    private var currentGroup: String? = null

    init {
        fetchAnalyticsGroup {
            fetchAnalytics(
                _chipItems.value
                    ?.getOrNull()
                    ?.firstOrNull()
                    ?.uid,
            )
        }
    }

    private fun fetchAnalyticsGroup(onSuccess: () -> Unit) {
        AnalyticsCountingIdlingResource.increment()
        viewModelScope.launch(dispatchers.io()) {
            try {
                val groups = charts.getVisualizationGroups(uid).map {
                    AnalyticGroup(it.id(), it.name())
                }
                _chipItems.value = Result.success(groups)
                onSuccess()
            } catch (e: Exception) {
                _chipItems.value = Result.failure(e)
            }
            finally {
                AnalyticsCountingIdlingResource.decrement()
            }
        }
    }

    fun filterByOrgUnit(
        chartModel: ChartModel,
        orgUnits: List<OrganisationUnit>,
        orgUnitFilterType: OrgUnitFilterType,
        lineListingColumnInd: Int?,
    ) {
        chartModel.graph.visualizationUid?.let {
            charts.setVisualizationOrgUnits(
                chartModel.graph.visualizationUid,
                lineListingColumnInd,
                orgUnits,
                orgUnitFilterType,
            )
            fetchAnalytics(currentGroup)
        }
    }

    fun filterByPeriod(
        chartModel: ChartModel,
        periods: List<RelativePeriod>,
        lineListingColumnInd: Int?,
    ) {
        chartModel.graph.visualizationUid?.let {
            charts.setVisualizationPeriods(
                chartModel.graph.visualizationUid,
                lineListingColumnInd,
                periods,
            )
            fetchAnalytics(currentGroup)
        }
    }

    fun filterLineListingRows(
        chartModel: ChartModel,
        column: Int,
        filterValue: String?,
    ) {
        chartModel.graph.visualizationUid?.let {
            charts.setLineListingFilter(it, column, filterValue)
            fetchAnalytics(currentGroup)
        }
    }

    fun resetFilter(
        chartModel: ChartModel,
        filterType: ChartFilter,
    ) {
        chartModel.graph.visualizationUid?.let {
            when (filterType) {
                ChartFilter.PERIOD ->
                    charts.setVisualizationPeriods(
                        chartModel.graph.visualizationUid,
                        null,
                        emptyList(),
                    )

                ChartFilter.ORG_UNIT ->
                    charts.setVisualizationOrgUnits(
                        chartModel.graph.visualizationUid,
                        null,
                        emptyList(),
                        OrgUnitFilterType.NONE,
                    )

                ChartFilter.COLUMN ->
                    charts.setLineListingFilter(
                        chartModel.graph.visualizationUid,
                        -1,
                        null,
                    )
            }
            fetchAnalytics(currentGroup)
        }
    }

    fun fetchAnalytics(groupUid: String?) {
        AnalyticsCountingIdlingResource.increment()
        currentGroup = groupUid
        when (mode) {
            AnalyticMode.ENROLLMENT ->
                viewModelScope.launch {
                    uid?.let {
                        getEnrollmentAnalyticsUseCase(uid)
                            .onSuccess { graphs ->
                                _analytics.value = Result.success(graphs.map { ChartModel(it) })
                            }.onFailure { e ->
                                Timber.e(e)
                                _analytics.value = Result.failure(e)
                            }
                    } ?: run {
                        _analytics.value = Result.success(emptyList())
                    }
                }

            else ->
                viewModelScope.launch(dispatchers.io()) {
                    try {
                        val result = when (mode) {
                            AnalyticMode.TRACKER_PROGRAM ->
                                uid?.let {
                                    charts.getProgramVisualizations(groupUid, uid)
                                        .map { ChartModel(it) }
                                } ?: emptyList()

                            AnalyticMode.EVENT_PROGRAM ->
                                uid?.let {
                                    charts.getProgramVisualizations(groupUid, uid)
                                        .map { ChartModel(it) }
                                } ?: emptyList()

                            AnalyticMode.HOME ->
                                charts.getHomeVisualizations(groupUid).map { ChartModel(it) }

                            AnalyticMode.DATASET ->
                                uid?.let {
                                    charts.getDataSetVisualizations(groupUid, uid)
                                        .map { ChartModel(it) }
                                } ?: emptyList()

                            AnalyticMode.ENROLLMENT -> emptyList()
                        }
                        _analytics.value = Result.success(result)
                    } catch (e: Exception) {
                        Timber.e(e)
                        _analytics.value = Result.failure(e)
                    }
                }
        }
    }

    fun trackAnalyticsPeriodFilter(mode: AnalyticMode) {
        matomoAnalyticsController.trackEvent(
            analyticsCategory(mode),
            Actions.ANALYTICS_FILTERS,
            Labels.PERIOD_FILTER,
        )
    }

    fun trackAnalyticsOrgUnitFilter(mode: AnalyticMode) {
        matomoAnalyticsController.trackEvent(
            analyticsCategory(mode),
            Actions.ANALYTICS_FILTERS,
            Labels.ORG_UNIT_FILTER,
        )
    }

    fun trackAnalyticsFilterReset(mode: AnalyticMode) {
        matomoAnalyticsController.trackEvent(
            analyticsCategory(mode),
            Actions.ANALYTICS_FILTERS,
            Labels.RESET_FILTER,
        )
    }

    fun trackChartTypeChanged(mode: AnalyticMode) {
        matomoAnalyticsController.trackEvent(
            analyticsCategory(mode),
            Actions.VISUALIZATION_CHANGE,
            Labels.CLICK,
        )
    }

    private fun analyticsCategory(mode: AnalyticMode): String =
        when (mode) {
            AnalyticMode.ENROLLMENT -> Categories.DASHBOARD
            AnalyticMode.HOME -> Categories.HOME
            AnalyticMode.DATASET -> Categories.DATASET_LIST
            AnalyticMode.EVENT_PROGRAM -> Categories.EVENT_LIST
            AnalyticMode.TRACKER_PROGRAM -> Categories.SEARCH
        }
}
