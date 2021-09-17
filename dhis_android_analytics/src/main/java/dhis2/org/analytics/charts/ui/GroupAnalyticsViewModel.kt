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
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

const val MIN_SIZE_TO_SHOW = 2

class GroupAnalyticsViewModel(
    private val mode: AnalyticMode,
    private val uid: String?,
    private val charts: Charts
) : ViewModel() {

    private val _chipItems = MutableLiveData<List<AnalyticGroup>>()
    val chipItems: LiveData<List<AnalyticGroup>> = _chipItems
    private val _analytics = MutableLiveData<List<AnalyticsModel>>()
    val analytics: LiveData<List<AnalyticsModel>> = _analytics
    private var currentGroup: String? = null

    init {
        fetchAnalyticsGroup()
        fetchAnalytics(_chipItems.value?.firstOrNull()?.uid)
    }

    private fun fetchAnalyticsGroup() {
        viewModelScope.launch {
            val result = async(context = Dispatchers.IO) {
                charts.getVisualizationGroups(uid).map {
                    AnalyticGroup(it.id(), it.name())
                }
            }
            _chipItems.value = result.await()
        }
    }

    fun filterByOrgUnit(
        chartModel: ChartModel,
        orgUnits: List<OrganisationUnit>,
        orgUnitFilterType: OrgUnitFilterType
    ) {
        chartModel.graph.visualizationUid?.let {
            charts.setVisualizationOrgUnits(
                chartModel.graph.visualizationUid, orgUnits,
                orgUnitFilterType
            )
            fetchAnalytics(currentGroup)
        }
    }

    fun filterByPeriod(chartModel: ChartModel, periods: List<RelativePeriod>) {
        chartModel.graph.visualizationUid?.let {
            charts.setVisualizationPeriods(chartModel.graph.visualizationUid, periods)
            fetchAnalytics(currentGroup)
        }
    }

    fun resetFilter(chartModel: ChartModel, filterType: ChartFilter) {
        chartModel.graph.visualizationUid?.let {
            when (filterType) {
                ChartFilter.PERIOD -> charts.setVisualizationPeriods(
                    chartModel.graph.visualizationUid,
                    emptyList()
                )
                ChartFilter.ORG_UNIT -> charts.setVisualizationOrgUnits(
                    chartModel.graph.visualizationUid,
                    emptyList(),
                    OrgUnitFilterType.NONE
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
                    AnalyticMode.PROGRAM -> uid?.let {
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
            _analytics.value = result.await()
        }
    }
}
