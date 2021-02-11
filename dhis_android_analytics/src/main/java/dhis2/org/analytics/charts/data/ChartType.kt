package dhis2.org.analytics.charts.data

import androidx.annotation.DrawableRes
import dhis2.org.R

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
