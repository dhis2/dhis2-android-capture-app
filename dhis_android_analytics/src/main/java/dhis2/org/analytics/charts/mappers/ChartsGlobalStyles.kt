package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.LineDataSet

const val default_value_text_size = 10f

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
        barWidth
    }
}

fun Legend.withGlobalStyle(): Legend {
    return apply {
        form = Legend.LegendForm.LINE
        horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
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
