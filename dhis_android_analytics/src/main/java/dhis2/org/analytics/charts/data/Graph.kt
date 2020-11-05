package dhis2.org.analytics.charts.data

import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

data class Graph(
    val title: String,
    val isOnline: Boolean,
    val coordinates: List<GraphPoint>,
    val periodToDisplay: String,
    val eventPeriodType: PeriodType,
    val periodStep: Long
) {
    fun numberOfStepsToDate(date: Date): Float {
        return if (coordinates.isEmpty()) {
            return 0f
        } else {
            ((date.time - coordinates.first().eventDate.time) / periodStep).toFloat()
        }
    }

    fun numberOfStepsToLastDate():Float {
        return if(coordinates.isEmpty()){
            return 0f
        }else{
            numberOfStepsToDate(coordinates.last().eventDate)
        }
    }

    fun dateFromSteps(numberOfSteps: Long): Date? {
        return if(coordinates.isEmpty()){
            return null
        } else {
            Date(coordinates.first().eventDate.time + numberOfSteps * periodStep)
        }
    }

    fun maxValue(): GraphPoint? {
        return coordinates.maxBy { it.fieldValue }
    }

    fun minValue(): GraphPoint? {
        return coordinates.minBy { it.fieldValue }
    }
}

data class GraphPoint(
    val eventDate: Date,
    val fieldValue: Float
)

fun Graph.toChartBuilder(): Chart.ChartBuilder {
    return Chart.ChartBuilder()
}