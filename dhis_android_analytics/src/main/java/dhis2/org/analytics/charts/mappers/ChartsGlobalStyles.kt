package dhis2.org.analytics.charts.mappers

import android.graphics.Typeface
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.RadarDataSet

const val DEFAULT_VALUE_TEXT_SIZE = 10f
const val SELECTED_VALUE_TEXT_SIZE = 14f
const val DEFAULT_BAR_GROUP_SEPARATION = 0.03f
const val DEFAULT_BAR_GROUP_SPACE = 1.0f - DEFAULT_BAR_GROUP_SEPARATION
const val DEFAULT_GAP = 1.0f

fun LineDataSet.withGlobalStyle(): LineDataSet =
    this.apply {
        lineWidth = 2.5f
        circleRadius = 5f
        circleHoleRadius = 2.5f
        valueTextSize = DEFAULT_VALUE_TEXT_SIZE
    }

fun BarData.withGlobalStyle(width: Float?): BarData =
    this.apply {
        setValueTextSize(DEFAULT_VALUE_TEXT_SIZE)
        when {
            dataSetCount > 1 ->
                barWidth = DEFAULT_BAR_GROUP_SPACE / dataSetCount.toFloat()
            width != null ->
                barWidth = width
        }
    }

fun PieData.withGlobalStyle(
    valueFormatter: PercentageValueFormatter,
    textColor: Int,
): PieData =
    this.apply {
        setValueFormatter(valueFormatter)
        setValueTextSize(11f)
        setValueTextColor(textColor)
    }

fun RadarDataSet.withGlobalStyle(): RadarDataSet =
    this.apply {
        lineWidth = 2.5f
        valueTextSize = DEFAULT_VALUE_TEXT_SIZE
    }

fun RadarDataSet.withHighlightStyle(): RadarDataSet =
    this.apply {
        lineWidth = 2.5f
        valueTypeface = Typeface.DEFAULT_BOLD
        valueTextSize = SELECTED_VALUE_TEXT_SIZE
    }

fun Legend.withGlobalStyle(): Legend =
    apply {
        form = Legend.LegendForm.LINE
        horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        isWordWrapEnabled = true
    }

fun LineDataSet.withNutritionBackgroundGlobalStyle(dataSetColor: Int): LineDataSet =
    this.apply {
        fillColor = dataSetColor
        fillAlpha = 200
        color = dataSetColor
        setDrawFilled(true)
        setDrawValues(false)
        setDrawCircles(false)
    }
