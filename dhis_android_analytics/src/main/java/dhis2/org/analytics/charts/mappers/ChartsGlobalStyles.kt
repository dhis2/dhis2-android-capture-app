package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.LineDataSet

fun LineDataSet.withGlobalStyle(): LineDataSet {
    return this.apply {
        lineWidth = 2.5f
        circleRadius = 5f
        circleHoleRadius = 2.5f
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
