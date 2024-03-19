package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphFilters
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.period.PeriodType

class DataElementToGraph(
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider,
) {
    fun map(
        dataElement: DataElement,
        stageUid: String,
        teiUid: String,
        stagePeriod: PeriodType,
        selectedRelativePeriod: List<RelativePeriod>?,
        selectedOrgUnits: List<String>?,
        isDefault: Boolean = false,
    ): Graph {
        val coordinates = chartCoordinatesProvider.dataElementCoordinates(
            stageUid,
            teiUid,
            dataElement.uid(),
            selectedRelativePeriod,
            selectedOrgUnits,
            isDefault,
        )

        val serie = if (coordinates.isNotEmpty()) {
            listOf(
                SerieData(
                    dataElement.displayFormName() ?: dataElement.uid(),
                    coordinates,
                ),
            )
        } else {
            emptyList()
        }

        return Graph(
            title = "${stagePeriod.name}-${dataElement.displayFormName()}",
            series = serie,
            periodToDisplayDefault = null,
            eventPeriodType = stagePeriod,
            periodStep = periodStepProvider.periodStep(stagePeriod),
            visualizationUid = "${teiUid}${stageUid}${dataElement.uid()}",
            graphFilters = GraphFilters.Visualization(
                periodToDisplaySelected = selectedRelativePeriod?.firstOrNull(),
                orgUnitsSelected = selectedOrgUnits ?: emptyList(),
            ),
        )
    }
}
