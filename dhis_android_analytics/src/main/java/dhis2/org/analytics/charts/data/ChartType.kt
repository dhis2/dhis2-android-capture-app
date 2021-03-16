package dhis2.org.analytics.charts.data

import androidx.annotation.DrawableRes
import dhis2.org.R
import org.hisp.dhis.android.core.settings.WHONutritionChartType

enum class ChartType(@DrawableRes val iconResource: Int) {
    LINE_CHART(R.drawable.ic_line_chart),
    BAR_CHART(R.drawable.ic_bar_chart),
    TABLE(R.drawable.ic_table_chart),
    SINGLE_VALUE(R.drawable.ic_single_value),
    NUTRITION(R.drawable.ic_line_chart)
}

enum class NutritionChartType {
    WHO_WFA_BOY,
    WHO_WFA_GIRL,
    WHO_HFA_BOY,
    WHO_HFA_GIRL,
    WHO_WFH_BOY,
    WHO_WHO_WFH_GIRL
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
