package dhis2.org.analytics.charts.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class CategoryFormatter(val categoryLabels: List<String>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return if (value >= 0 && value < categoryLabels.size) {
            categoryLabels[value.toInt()]
        } else {
            ""
        }
    }
}
