package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphFilters
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator

class ProgramIndicatorToGraph(
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider,
) {
    fun map(
        programIndicator: ProgramIndicator,
        stageUid: String,
        teiUid: String,
        stagePeriod: PeriodType,
        selectedRelativePeriod: List<RelativePeriod>?,
        selectedOrgUnits: List<String>?,
        isDefault: Boolean = false,
    ): Graph {
        val coordinates = chartCoordinatesProvider.indicatorCoordinates(
            stageUid,
            teiUid,
            programIndicator.uid(),
            selectedRelativePeriod,
            selectedOrgUnits,
            isDefault,
        )

        val serie = if (coordinates.isNotEmpty()) {
            listOf(
                SerieData(
                    programIndicator.displayName() ?: programIndicator.uid(),
                    coordinates,
                ),
            )
        } else {
            emptyList()
        }

        return Graph(
            title = "${stagePeriod.name}-${programIndicator.displayName()}",
            series = serie,
            periodToDisplayDefault = null,
            eventPeriodType = stagePeriod,
            periodStep = periodStepProvider.periodStep(stagePeriod),
            visualizationUid = "${teiUid}${stageUid}${programIndicator.uid()}",
            graphFilters = GraphFilters.Visualization(
                periodToDisplaySelected = selectedRelativePeriod?.firstOrNull(),
                orgUnitsSelected = selectedOrgUnits ?: emptyList(),
            ),
        )
    }
}
