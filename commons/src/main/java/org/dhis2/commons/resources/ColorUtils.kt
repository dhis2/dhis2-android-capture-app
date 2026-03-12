package org.dhis2.commons.resources

import android.content.Context
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import org.dhis2.commons.R
import java.util.Objects
import kotlin.math.pow
import androidx.compose.ui.graphics.Color as ComposeColor

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
        return newHexColor.toColorInt()
    }

    fun withAlpha(color: Int): Int = ColorUtils.setAlphaComponent(color, 155)

    fun withAlpha(
        color: Int,
        alpha: Int,
    ): Int = ColorUtils.setAlphaComponent(color, alpha)

    fun getColorFrom(
        hexColor: String?,
        defaultPrimaryColor: Int,
    ): Int {
        var colorToReturn = BLACK

        if (!hexColor.isNullOrEmpty()) {
            colorToReturn = parseColor(Objects.requireNonNull(hexColor))
        }
        if (hexColor.isNullOrEmpty() || colorToReturn == BLACK || colorToReturn == WHITE) {
            colorToReturn = defaultPrimaryColor
        }
        return colorToReturn
    }

    fun tintDrawableReosurce(
        drawableToTint: Drawable,
        bgResource: Int,
    ): Drawable {
        drawableToTint.setTint(getContrastColor(bgResource))
        drawableToTint.setTintMode(PorterDuff.Mode.SRC_IN)
        return drawableToTint
    }

    fun getContrastColor(color: Int): Int =
        if (getContrast(color) > 0.179) {
            "#b3000000".toColorInt()
        } else {
            "#e6ffffff".toColorInt()
        }

    fun getAlphaContrastColor(color: Int): Int =
        if (getContrast(color) > 0.500) {
            "#b3000000".toColorInt()
        } else {
            "#e6ffffff".toColorInt()
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
            if (it <= 0.03928) it / 12.92 else ((it + 0.055) / 1.055).pow(2.4)
            if (red == null) {
                red = it
            } else if (green == null) {
                green = it
            } else {
                blue = it
            }
        }
        return 0.2126 * red!! + 0.7152 * green!! + 0.0722 * blue!!
    }

    fun getThemeFromColor(color: String?): Int = paletteThemes[color] ?: -1

    //
    fun getPrimaryColor(
        context: Context,
        colorType: ColorType,
    ): Int = context.getPrimaryColor(colorType)

    fun getThemePrimaryColor(context: Context): ComposeColor {
        val typedValue = TypedValue()
        val a = context.theme.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.colorPrimary))
        val color = a.getColor(0, 0)
        a.recycle()
        return ComposeColor(color)
    }
}

enum class ColorType {
    PRIMARY,
    PRIMARY_LIGHT,
    PRIMARY_DARK,
    ACCENT,
}

fun Context.getPrimaryColor(colorType: ColorType): Int {
    val id =
        when (colorType) {
            ColorType.ACCENT -> R.attr.colorAccent
            ColorType.PRIMARY_DARK -> R.attr.colorPrimaryDark
            ColorType.PRIMARY_LIGHT -> R.attr.colorPrimaryLight
            ColorType.PRIMARY -> R.attr.colorPrimary
        }
    val typedValue = TypedValue()
    val a = obtainStyledAttributes(typedValue.data, intArrayOf(id))
    val colorToReturn = a.getColor(0, 0)
    a.recycle()
    return colorToReturn
}
