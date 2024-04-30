package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class PercentageValueFormatter(val pieChart: PieChart) : PercentFormatter(pieChart) {

    override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
        val yValue = pieEntry?.y?.toInt() ?: 0
        val valueToShow = when {
            yValue > 9999999 -> yValue.toString().removeRange(
                yValue.toString().length - 6,
                yValue.toString().length,
            ) + "M"
            yValue > 9999 ->
                yValue.toString()
                    .removeRange(
                        yValue.toString().length - 3,
                        yValue.toString().length,
                    ) + "k"
            else -> yValue.toString()
        }
        return valueToShow.plus(" (${super.getPieLabel(value, pieEntry)})")
    }
}
