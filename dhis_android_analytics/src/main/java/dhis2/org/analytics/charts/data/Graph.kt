package dhis2.org.analytics.charts.data

import java.util.Date
import org.hisp.dhis.android.core.period.PeriodType

data class Graph(
    val title: String,
    val isOnline: Boolean,
    val coordinates: List<List<GraphPoint>>,
    val periodToDisplay: String,
    val eventPeriodType: PeriodType,
    val periodStep: Long,
    val chartType: ChartType? = ChartType.LINE_CHART
) {
    fun numberOfStepsToDate(date: Date): Float {
        return if (coordinates.first().isEmpty()) {
            return 0f
        } else {
            ((date.time - coordinates.first().first().eventDate.time) / periodStep).toFloat()
        }
    }

    fun numberOfStepsToLastDate(): Float {
        return if (coordinates.first().isEmpty()) {
            return 0f
        } else {
            numberOfStepsToDate(coordinates.first().last().eventDate)
        }
    }

    fun dateFromSteps(numberOfSteps: Long): Date? {
        return if (coordinates.first().isEmpty()) {
            return null
        } else {
            Date(coordinates.first().first().eventDate.time + numberOfSteps * periodStep)
        }
    }

    fun maxValue(): Float {
        return coordinates.map {points-> points.map { it.fieldValue }.max()?:0f }.max()?:0f
    }

    fun minValue(): Float {
        return coordinates.map {points-> points.map { it.fieldValue }.min()?:0f }.min()?:0f
    }
}

data class GraphPoint(
    val eventDate: Date,
    val position: Int? = -1,
    val fieldValue: Float
)

fun Graph.toChartBuilder(): Chart.ChartBuilder {
    return Chart.ChartBuilder()
}
