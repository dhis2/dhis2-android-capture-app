package dhis2.org.analytics.charts.data

data class SettingsAnalyticModel(
    val displayName: String,
    val programUid: String,
    val type: ChartType,
    val dataElements: List<DataElementData>,
    val indicators: List<IndicatorData>,
    val period: String
)

data class DataElementData(val stageUid: String, val dataElementUid: String)
data class IndicatorData(val stageUid: String, val indicatorUid: String)
