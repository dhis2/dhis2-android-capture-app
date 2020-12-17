package dhis2.org.analytics.charts.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DateFormat
import java.util.Date

class AgeInMonthLabelFormatter() : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return value.toString()
    }
}
