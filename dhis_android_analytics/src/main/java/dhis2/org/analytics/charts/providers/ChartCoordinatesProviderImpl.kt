package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.GraphPoint
import org.hisp.dhis.android.core.D2
import java.text.SimpleDateFormat
import java.util.Date

class ChartCoordinatesProviderImpl(val d2: D2) : ChartCoordinatesProvider {

    override fun dataElementCoordinates(
        stageUid: String,
        teiUid: String,
        dataElementUid: String
    ): List<GraphPoint> {
        return d2.analyticsModule().eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
            .withDataElement(dataElementUid)
            .blockingEvaluate()
            .sortedBy { it.date }
            .mapNotNull { lineListResponse ->
                lineListResponse.values.first().value?.let { value ->
                    GraphPoint(
                        eventDate = formattedDate(lineListResponse.date),
                        fieldValue = value.toFloat()
                    )
                }
            }
    }

    override fun indicatorCoordinates(
        stageUid: String,
        teiUid: String,
        indicatorUid: String
    ): List<GraphPoint> {
        return d2.analyticsModule()
            .eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
            .withProgramIndicator(indicatorUid)
            .blockingEvaluate()
            .sortedBy { it.date }
            .filter {
                !(it.values.first().value?.toFloat() ?: Float.NaN).isNaN()
            }
            .mapNotNull { lineListResponse ->
                lineListResponse.values.first().value?.let { value ->
                    GraphPoint(
                        eventDate = formattedDate(lineListResponse.date),
                        fieldValue = value.toFloat()
                    )
                }
            }

    }

    private fun formattedDate(date: Date): Date {
        return try {
            val formattedDateString = SimpleDateFormat("yyyy-MM-dd").format(date)
            val formattedDate = SimpleDateFormat("yyyy-MM-dd").parse(formattedDateString)
            formattedDate ?: date
        } catch (e: Exception) {
            date
        }
    }

}