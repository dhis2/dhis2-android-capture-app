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
)

data class GraphPoint(
    val eventDate: Date,
    val fieldValue: Float
)