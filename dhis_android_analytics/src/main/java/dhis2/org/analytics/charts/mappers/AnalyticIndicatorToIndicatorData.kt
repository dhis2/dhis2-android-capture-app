package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.IndicatorData
import org.hisp.dhis.android.core.settings.AnalyticsTeiIndicator

class AnalyticIndicatorToIndicatorData {
    fun map(analyticsIndicator: AnalyticsTeiIndicator): IndicatorData {
        return IndicatorData(
            analyticsIndicator.programStage()!!,
            analyticsIndicator.indicator(),
        )
    }
}
