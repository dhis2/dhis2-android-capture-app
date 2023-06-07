package dhis2.org.analytics.charts.mappers

import android.graphics.Typeface
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.RadarDataSet

const val default_value_text_size = 10f
const val selected_value_text_size = 14f
const val default_bar_group_separation = 0.03f
const val default_bar_group_space = 1.0f - default_bar_group_separation
const val default_gap = 1.0f

fun LineDataSet.withGlobalStyle(): LineDataSet {
    return this.apply {
        lineWidth = 2.5f
        circleRadius = 5f
        circleHoleRadius = 2.5f
        valueTextSize = default_value_text_size
    }
}

fun BarData.withGlobalStyle(): BarData {
    return this.apply {
        setValueTextSize(default_value_text_size)
        if (dataSetCount > 1) {
            barWidth = default_bar_group_space / dataSetCount.toFloat()
        }
    }
}

fun PieData.withGlobalStyle(valueFormatter: PercentageValueFormatter, textColor: Int): PieData {
    return this.apply {
        setValueFormatter(valueFormatter)
        setValueTextSize(11f)
        setValueTextColor(textColor)
    }
}

fun RadarDataSet.withGlobalStyle(): RadarDataSet {
    return this.apply {
        lineWidth = 2.5f
        valueTextSize = default_value_text_size
    }
}

fun RadarDataSet.withHighlightStyle(): RadarDataSet {
    return this.apply {
        lineWidth = 2.5f
        valueTypeface = Typeface.DEFAULT_BOLD
        valueTextSize = selected_value_text_size
    }
}

fun Legend.withGlobalStyle(): Legend {
    return apply {
        form = Legend.LegendForm.LINE
        horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        isWordWrapEnabled = true
    }
}

fun LineDataSet.withNutritionBackgroundGlobalStyle(dataSetColor: Int): LineDataSet {
    return this.apply {
        fillColor = dataSetColor
        fillAlpha = 200
        color = dataSetColor
        setDrawFilled(true)
        setDrawValues(false)
        setDrawCircles(false)
    }
}
