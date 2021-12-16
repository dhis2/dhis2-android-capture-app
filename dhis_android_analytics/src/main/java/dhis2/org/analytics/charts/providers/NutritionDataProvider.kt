package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.NutritionChartType
import dhis2.org.analytics.charts.data.SerieData

interface NutritionDataProvider {
    fun getNutritionData(nutritionChartType: NutritionChartType): List<SerieData>
}
