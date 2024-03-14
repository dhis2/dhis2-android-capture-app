package dhis2.org.analytics.charts.data

import org.hisp.dhis.android.core.period.PeriodType

sealed class SettingsAnalyticModel(
    val displayName: String,
    val programUid: String,
    val type: ChartType,
) {
    open fun dataElements(): List<DataElementData> {
        return emptyList()
    }

    open fun indicators(): List<IndicatorData> {
        return emptyList()
    }

    open fun period(): String {
        return PeriodType.Daily.name
    }
}

data class NutritionSettingsAnalyticsModel(
    private val name: String,
    private val program: String,
    private val chartType: ChartType,
    val genderData: NutritionGenderData,
    val stageUid: String,
    val zScoreContainerUid: String,
    val zScoreContainerIsDataElement: Boolean,
    val ageOrHeightContainerUid: String,
    val ageOrHeightIsDataElement: Boolean,
) : SettingsAnalyticModel(name, program, chartType)

data class DefaultSettingsAnalyticModel(
    private val name: String,
    private val program: String,
    private val chartType: ChartType,
    val dataElementList: List<DataElementData>,
    val indicatorList: List<IndicatorData>,
    val stagePeriod: String,
) : SettingsAnalyticModel(name, program, chartType) {
    override fun dataElements(): List<DataElementData> {
        return dataElementList
    }

    override fun indicators(): List<IndicatorData> {
        return indicatorList
    }

    override fun period(): String {
        return stagePeriod
    }
}

data class DataElementData(val stageUid: String, val dataElementUid: String)
data class IndicatorData(val stageUid: String, val indicatorUid: String)
data class NutritionGenderData(
    val attributeUid: String,
    private val femaleValue: String,
    private val maleValue: String,
) {
    fun isFemale(value: String?): Boolean {
        return value == femaleValue
    }

    fun isMale(value: String?): Boolean {
        return value == maleValue
    }
}
