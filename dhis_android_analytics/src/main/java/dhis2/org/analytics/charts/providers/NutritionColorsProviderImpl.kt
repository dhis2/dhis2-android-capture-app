package dhis2.org.analytics.charts.providers

import android.graphics.Color

class NutritionColorsProviderImpl : NutritionColorsProvider {
    private val nutritionColors =
        listOf(
            Color.parseColor("#ff8a80"),
            Color.parseColor("#ffd180"),
            Color.parseColor("#ffff8d"),
            Color.parseColor("#b9f6ca"),
            Color.parseColor("#ffff8d"),
            Color.parseColor("#ffd180"),
            Color.parseColor("#ff8a80"),
        )

    override fun nutritionColors(): List<Int> = nutritionColors

    override fun getColorAt(position: Int): Int = nutritionColors[position]
}
