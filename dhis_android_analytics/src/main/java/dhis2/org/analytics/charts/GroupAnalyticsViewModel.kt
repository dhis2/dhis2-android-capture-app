package dhis2.org.analytics.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dhis2.org.analytics.charts.data.AnalyticGroup

const val MIN_SIZE_TO_SHOW = 2

class GroupAnalyticsViewModel : ViewModel() {

    private val _chipItems = MutableLiveData<List<AnalyticGroup>>()
    val chipItems: LiveData<List<AnalyticGroup>> = _chipItems

    init {
        fetchAnalyticsGroup()
    }

    fun fetchAnalyticsGroup() {
        val analytics = listOf(
            AnalyticGroup("1", "Group 1"),
            AnalyticGroup("2", "Group 2"),
            AnalyticGroup("3", "Group 3")
        )
        _chipItems.value = analytics
    }
}