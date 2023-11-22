package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.DataElementData
import org.hisp.dhis.android.core.settings.AnalyticsTeiDataElement

class AnalyticDataElementToDataElementData {
    fun map(analyticDataElement: AnalyticsTeiDataElement): DataElementData {
        return DataElementData(
            analyticDataElement.programStage()!!,
            analyticDataElement.dataElement(),
        )
    }
}
