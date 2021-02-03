package dhis2.org.analytics.charts.data

import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

data class Graph(
    val title: String,
    val isOnline: Boolean,
    val series: List<SerieData>,
    val periodToDisplay: String,
    val eventPeriodType: PeriodType,
    val periodStep: Long,
    val chartType: ChartType? = ChartType.LINE_CHART
) {
    fun numberOfStepsToDate(date: Date): Float {
        return if (series.first().coordinates.isEmpty()) {
            return 0f
        } else {
            ((date.time - series.first().coordinates.first().eventDate.time) / periodStep).toFloat()
        }
    }

    fun numberOfStepsToLastDate(): Float {
        return if (series.first().coordinates.isEmpty()) {
            return 0f
        } else {
            numberOfStepsToDate(series.first().coordinates.last().eventDate)
        }
    }

    fun dateFromSteps(numberOfSteps: Long): Date? {
        return if (series.first().coordinates.isEmpty()) {
            return null
        } else {
            Date(series.first().coordinates.first().eventDate.time + numberOfSteps * periodStep)
        }
    }

    fun maxValue(): Float {
        return series.map { it.coordinates.map { points -> points.fieldValue }.max() ?: 0f }.max()
            ?: 0f
    }

    fun minValue(): Float {
        return series.map { it.coordinates.map { points -> points.fieldValue }.min() ?: 0f }.min()
            ?: 0f
    }
}

data class SerieData(
    val fieldName: String,
    val coordinates: List<GraphPoint>
)

data class GraphPoint(
    val eventDate: Date,
    val position: Int? = -1,
    val fieldValue: Float
)

fun Graph.toChartBuilder(): Chart.ChartBuilder {
    return Chart.ChartBuilder()
}
