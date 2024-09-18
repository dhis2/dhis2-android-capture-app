package dhis2.org.analytics.charts.data

import androidx.annotation.DrawableRes
import dhis2.org.R
import org.hisp.dhis.android.core.settings.WHONutritionChartType
import org.hisp.dhis.android.core.visualization.TrackerVisualizationType
import org.hisp.dhis.android.core.visualization.VisualizationType

enum class ChartType(@DrawableRes val iconResource: Int) {
    LINE_CHART(R.drawable.ic_line_chart),
    BAR_CHART(R.drawable.ic_bar_chart),
    TABLE(R.drawable.ic_table_chart),
    SINGLE_VALUE(R.drawable.ic_single_value),
    NUTRITION(R.drawable.ic_line_chart),
    RADAR(R.drawable.ic_radar_chart),
    PIE_CHART(R.drawable.ic_pie_chart),
    LINE_LISTING(R.drawable.ic_table_chart),
}

enum class NutritionChartType {
    WHO_WFA_BOY,
    WHO_WFA_GIRL,
    WHO_HFA_BOY,
    WHO_HFA_GIRL,
    WHO_WFH_BOY,
    WHO_WHO_WFH_GIRL,
}

fun org.hisp.dhis.android.core.settings.ChartType.toAnalyticsChartType(): ChartType {
    return when (this) {
        org.hisp.dhis.android.core.settings.ChartType.BAR -> ChartType.BAR_CHART
        org.hisp.dhis.android.core.settings.ChartType.LINE -> ChartType.LINE_CHART
        org.hisp.dhis.android.core.settings.ChartType.TABLE -> ChartType.TABLE
        org.hisp.dhis.android.core.settings.ChartType.WHO_NUTRITION -> ChartType.NUTRITION
        org.hisp.dhis.android.core.settings.ChartType.SINGLE_VALUE -> ChartType.SINGLE_VALUE
    }
}

fun VisualizationType?.toAnalyticsChartType(): ChartType {
    return when (this) {
        VisualizationType.LINE -> ChartType.LINE_CHART
        VisualizationType.COLUMN,
        VisualizationType.STACKED_COLUMN,
        VisualizationType.BAR,
        VisualizationType.STACKED_BAR,
        -> ChartType.BAR_CHART

        VisualizationType.PIE -> ChartType.PIE_CHART
        VisualizationType.RADAR -> ChartType.RADAR
        VisualizationType.SINGLE_VALUE -> ChartType.SINGLE_VALUE
        else -> ChartType.TABLE
    }
}

fun TrackerVisualizationType?.toAnalyticsChartType(): ChartType {
    return ChartType.LINE_LISTING
}

fun WHONutritionChartType.toNutritionChartType(isFemale: Boolean): NutritionChartType {
    return when (this) {
        WHONutritionChartType.WFA ->
            if (isFemale) NutritionChartType.WHO_WFA_GIRL else NutritionChartType.WHO_WFA_BOY

        WHONutritionChartType.WFH ->
            if (isFemale) NutritionChartType.WHO_WHO_WFH_GIRL else NutritionChartType.WHO_WFH_BOY

        WHONutritionChartType.HFA ->
            if (isFemale) NutritionChartType.WHO_HFA_GIRL else NutritionChartType.WHO_HFA_BOY
    }
}
