package dhis2.org.analytics.charts.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.data.AnalyticGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

const val MIN_SIZE_TO_SHOW = 2

class GroupAnalyticsViewModel(
    private val mode: AnalyticMode,
    private val uid: String?,
    private val charts: Charts,
    private val matomoAnalyticsController: MatomoAnalyticsController,
) : ViewModel() {

    private val _chipItems = MutableLiveData<Result<List<AnalyticGroup>>>()
    val chipItems: LiveData<Result<List<AnalyticGroup>>> = _chipItems
    private val _analytics = MutableLiveData<Result<List<AnalyticsModel>>>()
    val analytics: LiveData<Result<List<AnalyticsModel>>> = _analytics
    private var currentGroup: String? = null

    init {
        fetchAnalyticsGroup {
            fetchAnalytics(_chipItems.value?.getOrNull()?.firstOrNull()?.uid)
        }
    }

    private fun fetchAnalyticsGroup(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = async(context = Dispatchers.IO) {
                charts.getVisualizationGroups(uid).map {
                    AnalyticGroup(it.id(), it.name())
                }
            }
            try {
                _chipItems.value = Result.success(result.await())
                onSuccess()
            } catch (e: Exception) {
                _chipItems.value = Result.failure(e)
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

    fun filterLineListingRows(chartModel: ChartModel, column: Int, filterValue: String?) {
        chartModel.graph.visualizationUid?.let {
            charts.setLineListingFilter(it, column, filterValue)
            fetchAnalytics(currentGroup)
        }
    }

    fun resetFilter(chartModel: ChartModel, filterType: ChartFilter) {
        chartModel.graph.visualizationUid?.let {
            when (filterType) {
                ChartFilter.PERIOD -> charts.setVisualizationPeriods(
                    chartModel.graph.visualizationUid,
                    null,
                    emptyList(),
                )

                ChartFilter.ORG_UNIT -> charts.setVisualizationOrgUnits(
                    chartModel.graph.visualizationUid,
                    null,
                    emptyList(),
                    OrgUnitFilterType.NONE,
                )

                ChartFilter.COLUMN -> charts.setLineListingFilter(
                    chartModel.graph.visualizationUid,
                    -1,
                    null,
                )
            }
            fetchAnalytics(currentGroup)
        }
    }

    fun fetchAnalytics(groupUid: String?) {
        currentGroup = groupUid
        viewModelScope.launch {
            val result = async(context = Dispatchers.IO) {
                when (mode) {
                    AnalyticMode.ENROLLMENT -> uid?.let {
                        charts.geEnrollmentCharts(uid)
                            .map { ChartModel(it) }
                    } ?: emptyList()

                    AnalyticMode.TRACKER_PROGRAM -> uid?.let {
                        charts.getProgramVisualizations(groupUid, uid)
                            .map { ChartModel(it) }
                    } ?: emptyList()

                    AnalyticMode.EVENT_PROGRAM -> uid?.let {
                        charts.getProgramVisualizations(groupUid, uid)
                            .map { ChartModel(it) }
                    } ?: emptyList()

                    AnalyticMode.HOME ->
                        charts.getHomeVisualizations(groupUid)
                            .map { ChartModel(it) }

                    AnalyticMode.DATASET -> uid?.let {
                        charts.getDataSetVisualizations(groupUid, uid)
                            .map { ChartModel(it) }
                    } ?: emptyList()
                }
            }
            try {
                _analytics.value = Result.success(result.await())
            } catch (e: Exception) {
                e.printStackTrace()
                _analytics.value = Result.failure(e)
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

    private fun analyticsCategory(mode: AnalyticMode): String {
        return when (mode) {
            AnalyticMode.ENROLLMENT -> Categories.DASHBOARD
            AnalyticMode.HOME -> Categories.HOME
            AnalyticMode.DATASET -> Categories.DATASET_LIST
            AnalyticMode.EVENT_PROGRAM -> Categories.EVENT_LIST
            AnalyticMode.TRACKER_PROGRAM -> Categories.SEARCH
        }
    }
}
