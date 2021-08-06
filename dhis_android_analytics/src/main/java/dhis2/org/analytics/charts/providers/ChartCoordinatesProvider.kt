package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.GraphPoint
import org.hisp.dhis.android.core.analytics.aggregated.GridResponseValue
import org.hisp.dhis.android.core.analytics.aggregated.MetadataItem

interface ChartCoordinatesProvider {
    fun dataElementCoordinates(
        stageUid: String,
        teiUid: String,
        dataElementUid: String
    ): List<GraphPoint>

    fun indicatorCoordinates(
        stageUid: String,
        teiUid: String,
        indicatorUid: String
    ): List<GraphPoint>

    fun nutritionCoordinates(
        stageUid: String,
        teiUid: String,
        zScoreValueContainerUid: String,
        zScoreSavedIsDataElement: Boolean,
        ageOrHeightCountainerUid: String,
        ageOrHeightIsDataElement: Boolean
    ): List<GraphPoint>

    fun pieChartCoordinates(
        stageUid: String,
        teiUid: String,
        dataElementUid: String
    ): List<GraphPoint>

    fun visualizationCoordinates(
        gridResponseValueList: List<GridResponseValue>,
        metadata: Map<String, MetadataItem>,
        categories: List<String>
    ): List<GraphPoint>
}
