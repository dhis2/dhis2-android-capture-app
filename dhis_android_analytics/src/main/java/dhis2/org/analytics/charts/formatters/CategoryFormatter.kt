package dhis2.org.analytics.charts.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class CategoryFormatter(val categoryLabels: List<String>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val index = value.takeIf { it > 0 } ?: 0f
        return categoryLabels[index.toInt() % categoryLabels.size]
    }
}
