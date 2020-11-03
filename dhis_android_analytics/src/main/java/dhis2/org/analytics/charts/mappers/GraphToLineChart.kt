package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import dhis2.org.analytics.charts.data.Graph
import java.text.DateFormat
import java.util.Date

const val DEFAULT_VALUE = 0f
const val VALUE_PADDING = 50f

fun Graph.toLineChart(context: Context): LineChart {
    return LineChart(context).apply {
        isDragEnabled = true
        setScaleEnabled(true)
        setPinchZoom(true)
        xAxis.apply {
            enableGridDashedLine(10f, 10f, 0f)
            setDrawLimitLinesBehindData(true)
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    val labelDate =
                        Date(coordinates.first().eventDate.time + value.toLong() * periodStep)
                    return DateFormat.getDateInstance().format(labelDate)
                }
            }
            granularity = 1f
            axisMinimum = -1f
            axisMaximum =
                ((coordinates.last().eventDate.time - coordinates.first().eventDate.time) / periodStep).toFloat()+1f
        }

        axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMaximum =
                (coordinates.maxBy { it.fieldValue }?.fieldValue ?: DEFAULT_VALUE) + VALUE_PADDING
            axisMinimum =
                (coordinates.minBy { it.fieldValue }?.fieldValue ?: DEFAULT_VALUE) - VALUE_PADDING
            setDrawLimitLinesBehindData(true)
        }
        axisRight.isEnabled = false

        animateX(1500)

        legend.apply {
            form = Legend.LegendForm.LINE
        }

        data = LineData(
            LineDataSet(
                coordinates.mapIndexed { index, graphPoint ->
                    Entry(
                        if (index > 0) {
                            ((graphPoint.eventDate.time - coordinates.first().eventDate.time) / periodStep).toFloat()
                        } else {
                            index.toFloat()
                        },
                        graphPoint.fieldValue
                    )
                },
                title
            )
        )

        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500)
    }
}
