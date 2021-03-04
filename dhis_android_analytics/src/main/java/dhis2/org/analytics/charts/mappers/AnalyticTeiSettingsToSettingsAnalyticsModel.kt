package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.SettingsAnalyticModel
import dhis2.org.analytics.charts.data.toAnalyticsChartType
import org.hisp.dhis.android.core.settings.AnalyticsTeiSetting

class AnalyticTeiSettingsToSettingsAnalyticsModel(
    private val analyticDataElementMapper: AnalyticDataElementToDataElementData,
    private val analyticIndicatorMapper: AnalyticIndicatorToIndicatorData
) {
    fun map(analyticsTeiSetting: AnalyticsTeiSetting): SettingsAnalyticModel {
        return SettingsAnalyticModel(
            analyticsTeiSetting.name(),
            analyticsTeiSetting.program(),
            analyticsTeiSetting.type().toAnalyticsChartType(),
            analyticsTeiSetting.data().dataElements().filter { it.programStage() != null }.map {
                analyticDataElementMapper.map(it)
            },
            analyticsTeiSetting.data().indicators().filter { it.programStage() != null }.map {
                analyticIndicatorMapper.map(it)
            },
            analyticsTeiSetting.period().name
        )
    }
}