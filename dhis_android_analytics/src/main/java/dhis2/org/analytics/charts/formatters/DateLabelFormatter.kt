package dhis2.org.analytics.charts.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DateFormat
import java.util.Date

const val EMPTY_LABEL = "-"
const val BLANK_LABEL = ""

class DateLabelFormatter(val dateFromValue: (Long) -> Date?) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val labelDate = dateFromValue(value.toLong())
        return if (value < 0f) {
            BLANK_LABEL
        } else {
            labelDate?.let { DateFormat.getDateInstance().format(it) } ?: EMPTY_LABEL
        }
    }
}
