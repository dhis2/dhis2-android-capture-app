package dhis2.org.analytics.charts.data

import android.graphics.Color
import org.dhis2.commons.resources.ColorUtils

class SerieColors {
    companion object {
        fun getColors(): List<Int> = listOf(
            Color.parseColor("#E91E63"),
            Color.parseColor("#673AB7"),
            Color.parseColor("#03A9F4"),
            Color.parseColor("#009688"),
            Color.parseColor("#8BC34A"),
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#795548"),
            Color.parseColor("#9E9E9E"),
            Color.parseColor("#607D8B"),

            Color.parseColor("#FF1744"),
            Color.parseColor("#D500F9"),
            Color.parseColor("#2979FF"),
            Color.parseColor("#00E676"),
            Color.parseColor("#C6FF00"),
            Color.parseColor("#FFC400"),
            Color.parseColor("#FF3D00"),

            Color.parseColor("#FF8A80"),
            Color.parseColor("#EA80FC"),
            Color.parseColor("#8C9EFF"),
            Color.parseColor("#80D8FF"),
            Color.parseColor("#84FFFF"),
            Color.parseColor("#B9F6CA"),
            Color.parseColor("#F4FF81"),
            Color.parseColor("#FFE57F"),

            Color.parseColor("#F50057"),
            Color.parseColor("#D500F9"),
            Color.parseColor("#00B0FF"),
            Color.parseColor("#00E5FF"),
            Color.parseColor("#1DE9B6"),
            Color.parseColor("#76FF03"),
            Color.parseColor("#FFEA00"),
            Color.parseColor("#FF9100"),
        )

        fun getSerieColor(colorIndex: Int, higlight: Boolean): Int {
            return if (higlight) {
                getColors()[colorIndex]
            } else {
                ColorUtils().withAlpha(getColors()[colorIndex], ColorUtils.ALPHA_20_PERCENT)
            }
        }
    }
}
