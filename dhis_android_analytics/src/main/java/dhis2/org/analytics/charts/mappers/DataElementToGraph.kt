package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.period.PeriodType

class DataElementToGraph(
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider
) {
    fun map(
        dataElement: DataElement,
        stageUid: String,
        teiUid: String,
        stagePeriod: PeriodType
    ): Graph {
        val coordinates = chartCoordinatesProvider.dataElementCoordinates(
            stageUid,
            teiUid,
            dataElement.uid()
        )

        val serie = if (coordinates.isNotEmpty()) {
            listOf(
                SerieData(
                    dataElement.displayFormName() ?: dataElement.uid(),
                    coordinates
                )
            )
        } else {
            emptyList()
        }

        return Graph(
            "${stagePeriod.name}-${dataElement.displayFormName()}",
            false,
            serie,
            "",
            stagePeriod,
            periodStepProvider.periodStep(stagePeriod)
        )
    }
}
