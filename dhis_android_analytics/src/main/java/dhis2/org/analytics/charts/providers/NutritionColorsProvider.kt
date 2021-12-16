package dhis2.org.analytics.charts.providers

interface NutritionColorsProvider {
    fun nutritionColors(): List<Int>
    fun getColorAt(position: Int): Int
}
