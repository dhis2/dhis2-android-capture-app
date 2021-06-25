package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class PercentageValueFormatter(val pieChart: PieChart) : PercentFormatter(pieChart) {

    override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
        return super.getPieLabel(value, pieEntry).plus(" (${pieEntry?.y?.toInt()})")
    }
}
