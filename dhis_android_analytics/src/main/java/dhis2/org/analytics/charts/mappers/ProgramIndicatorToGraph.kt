package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator

class ProgramIndicatorToGraph(
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider
) {
    fun map(
        programIndicator: ProgramIndicator,
        stageUid: String,
        teiUid: String,
        stagePeriod: PeriodType
    ): Graph {
        val coordinates = chartCoordinatesProvider.indicatorCoordinates(
            stageUid,
            teiUid,
            programIndicator.uid()
        )

        val serie = if (coordinates.isNotEmpty()) {
            listOf(
                SerieData(
                    programIndicator.displayName() ?: programIndicator.uid(),
                    coordinates
                )
            )
        } else {
            emptyList()
        }

        return Graph(
            "${stagePeriod.name}-${programIndicator.displayName()}",
            false,
            serie,
            "",
            stagePeriod,
            periodStepProvider.periodStep(stagePeriod)
        )
    }
}
