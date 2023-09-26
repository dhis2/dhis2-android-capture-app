package org.dhis2.commons.resources

import android.content.Context
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.graphics.ColorUtils
import org.dhis2.commons.R
import java.util.Objects

class ColorUtils {
    companion object {
        const val ALPHA_20_PERCENT = 51
    }
    fun parseColor(hexColor: String): Int {
        var newHexColor = hexColor
        if (hexColor.length == 4) { // Color is formatted as #fff
            val r = hexColor[1]
            val g = hexColor[2]
            val b = hexColor[3]
            newHexColor = "#$r$r$g$g$b$b" // formatted to #ffff
        }
        return Color.parseColor(newHexColor)
    }

    fun getPrimaryColorWithAlpha(context: Context, primaryLight: ColorType, alpha: Float): Int {
        val primaryColor = getPrimaryColor(context, primaryLight)
        return ColorUtils.setAlphaComponent(primaryColor, 155)
    }

    fun withAlpha(color: Int): Int {
        return ColorUtils.setAlphaComponent(color, 155)
    }

    fun withAlpha(color: Int, alpha: Int): Int {
        return ColorUtils.setAlphaComponent(color, alpha)
    }

    fun getColorFrom(hexColor: String?, defaultPrimaryColor: Int): Int {
        var colorToReturn = BLACK

        if (!hexColor.isNullOrEmpty()) {
            colorToReturn = parseColor(Objects.requireNonNull(hexColor))
        }
        if (hexColor.isNullOrEmpty() || colorToReturn == BLACK || colorToReturn == WHITE) {
            colorToReturn = defaultPrimaryColor
        }
        return colorToReturn
    }

    fun tintDrawableReosurce(drawableToTint: Drawable, bgResource: Int): Drawable {
        drawableToTint.setTint(getContrastColor(bgResource))
        drawableToTint.setTintMode(PorterDuff.Mode.SRC_IN)
        return drawableToTint
    }

    fun tintDrawableWithColor(drawableToTint: Drawable, tintColor: Int): Drawable {
        drawableToTint.setTint(tintColor)
        drawableToTint.setTintMode(PorterDuff.Mode.SRC_IN)
        return drawableToTint
    }

    fun getContrastColor(color: Int): Int {
        return if (getContrast(color) > 0.179) {
            Color.parseColor("#b3000000")
        } else {
            Color.parseColor("#e6ffffff")
        }
    }

    fun getAlphaContrastColor(color: Int): Int {
        return if (getContrast(color) > 0.500) {
            Color.parseColor("#b3000000")
        } else {
            Color.parseColor("#e6ffffff")
        }
    }

    private fun getContrast(color: Int): Double {
        val rgb = ArrayList<Double>()
        rgb.add(Color.red(color) / 255.0)
        rgb.add(Color.green(color) / 255.0)
        rgb.add(Color.blue(color) / 255.0)
        var red: Double? = null
        var green: Double? = null
        var blue: Double? = null
        rgb.forEach {
            if (it <= 0.03928) it / 12.92 else Math.pow((it + 0.055) / 1.055, 2.4)
            if (red == null) red = it else if (green == null) green = it else blue = it
        }
        return 0.2126 * red!! + 0.7152 * green!! + 0.0722 * blue!!
    }

    fun getThemeFromColor(color: String?): Int {
        return when (color) {
            "#ffcdd2" -> R.style.colorPrimary_Pink
            "#e57373" -> R.style.colorPrimary_e57
            "#d32f2f" -> R.style.colorPrimary_d32
            "#f06292" -> R.style.colorPrimary_f06
            "#c2185b" -> R.style.colorPrimary_c21
            "#880e4f" -> R.style.colorPrimary_880
            "#f50057" -> R.style.colorPrimary_f50
            "#e1bee7" -> R.style.colorPrimary_e1b
            "#ba68c8" -> R.style.colorPrimary_ba6
            "#8e24aa" -> R.style.colorPrimary_8e2
            "#aa00ff" -> R.style.colorPrimary_aa0
            "#7e57c2" -> R.style.colorPrimary_7e5
            "#4527a0" -> R.style.colorPrimary_452
            "#7c4dff" -> R.style.colorPrimary_7c4
            "#6200ea" -> R.style.colorPrimary_620
            "#c5cae9" -> R.style.colorPrimary_c5c
            "#7986cb" -> R.style.colorPrimary_798
            "#3949ab" -> R.style.colorPrimary_394
            "#304ffe" -> R.style.colorPrimary_304
            "#e3f2fd" -> R.style.colorPrimary_e3f
            "#64b5f6" -> R.style.colorPrimary_64b
            "#1976d2" -> R.style.colorPrimary_197
            "#0288d1" -> R.style.colorPrimary_028
            "#40c4ff" -> R.style.colorPrimary_40c
            "#00b0ff" -> R.style.colorPrimary_00b
            "#80deea" -> R.style.colorPrimary_80d
            "#00acc1" -> R.style.colorPrimary_00a
            "#00838f" -> R.style.colorPrimary_008
            "#006064" -> R.style.colorPrimary_006
            "#e0f2f1" -> R.style.colorPrimary_e0f
            "#80cbc4" -> R.style.colorPrimary_80c
            "#00695c" -> R.style.colorPrimary_0069
            "#64ffda" -> R.style.colorPrimary_64f
            "#c8e6c9" -> R.style.colorPrimary_c8e
            "#66bb6a" -> R.style.colorPrimary_66b
            "#2e7d32" -> R.style.colorPrimary_2e7
            "#60ad5e" -> R.style.colorPrimary_60a
            "#00e676" -> R.style.colorPrimary_00e
            "#aed581" -> R.style.colorPrimary_aed
            "#689f38" -> R.style.colorPrimary_689
            "#33691e" -> R.style.colorPrimary_336
            "#76ff03" -> R.style.colorPrimary_76f
            "#64dd17" -> R.style.colorPrimary_64d
            "#cddc39" -> R.style.colorPrimary_cdd
            "#9e9d24" -> R.style.colorPrimary_9e9
            "#827717" -> R.style.colorPrimary_827
            "#fff9c4" -> R.style.colorPrimary_fff
            "#fbc02d" -> R.style.colorPrimary_fbc
            "#f57f17" -> R.style.colorPrimary_f57
            "#ffff00" -> R.style.colorPrimary_ffff
            "#ffcc80" -> R.style.colorPrimary_ffc
            "#ffccbc" -> R.style.colorPrimary_ffcc
            "#ffab91" -> R.style.colorPrimary_ffa
            "#bcaaa4" -> R.style.colorPrimary_bca
            "#8d6e63" -> R.style.colorPrimary_8d6
            "#4e342e" -> R.style.colorPrimary_4e3
            "#fafafa" -> R.style.colorPrimary_faf
            "#bdbdbd" -> R.style.colorPrimary_bdb
            "#757575" -> R.style.colorPrimary_757
            "#424242" -> R.style.colorPrimary_424
            "#cfd8dc" -> R.style.colorPrimary_cfd
            "#b0bec5" -> R.style.colorPrimary_b0b
            "#607d8b" -> R.style.colorPrimary_607
            "#37474f" -> R.style.colorPrimary_374
            else -> -1
        }
    }

    /**/
    fun getPrimaryColor(context: Context, colorType: ColorType): Int {
        return context.getPrimaryColor(colorType)
    }
}

enum class ColorType {
    PRIMARY, PRIMARY_LIGHT, PRIMARY_DARK, ACCENT
}

fun Context.getPrimaryColor(colorType: ColorType): Int {
    val id = when (colorType) {
        ColorType.ACCENT -> R.attr.colorAccent
        ColorType.PRIMARY_DARK -> R.attr.colorPrimaryDark
        ColorType.PRIMARY_LIGHT -> R.attr.colorPrimaryLight
        ColorType.PRIMARY -> R.attr.colorPrimary
        else -> R.attr.colorPrimary
    }
    val typedValue = TypedValue()
    val a = obtainStyledAttributes(typedValue.data, intArrayOf(id))
    val colorToReturn = a.getColor(0, 0)
    a.recycle()
    return colorToReturn
}
