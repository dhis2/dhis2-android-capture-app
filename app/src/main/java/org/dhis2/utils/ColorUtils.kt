package org.dhis2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import org.dhis2.R
import org.dhis2.utils.ColorUtils
import java.util.ArrayList

class ColorUtils {
    companion object {
        @SuppressLint("NewApi")
        fun getRippleDrawable(pressedColor: Int, bgDrawable: Drawable): RippleDrawable {
            return RippleDrawable(getPressedState(pressedColor), bgDrawable, null)
        }

        fun getPressedState(pressedColor: Int): ColorStateList {
            return ColorStateList(arrayOf(intArrayOf()), intArrayOf(pressedColor))
        }

        fun getColorFrom(hexColor: String?, defaultPrimaryColor: Int): Int {
            var hexColor = hexColor
            var colorToReturn = Color.BLACK

            if (!hexColor.isNullOrEmpty()) {
                if (hexColor!!.length == 4) {//Color is formatted as #fff
                    val r = hexColor[1]
                    val g = hexColor[2]
                    val b = hexColor[3]
                    hexColor = "#$r$r$g$g$b$b" //formatted to #ffff
                }
                colorToReturn = Color.parseColor(hexColor)
            }
            if (hexColor.isNullOrEmpty() || colorToReturn == Color.BLACK || colorToReturn == Color.WHITE) {
                colorToReturn = defaultPrimaryColor
            }

            return colorToReturn
        }

        fun tintDrawableReosurce(drawableToTint: Drawable, bgResource: Int): Drawable {
            drawableToTint.setColorFilter(getContrastColor(bgResource), PorterDuff.Mode.SRC_IN)
            return drawableToTint
        }

        fun tintDrawableWithColor(drawableToTint: Drawable, tintColor: Int): Drawable {
            drawableToTint.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            return drawableToTint
        }

        fun getContrastColor(color: Int): Int {

            val rgb = ArrayList<Double>()
            rgb.add(Color.red(color) / 255.0)
            rgb.add(Color.green(color) / 255.0)
            rgb.add(Color.blue(color) / 255.0)

            var r: Double? = null
            var g: Double? = null
            var b: Double? = null
            for (c in rgb) {
                var cC = c;
                cC /= if (cC <= 0.03928)
                    12.92
                else
                    Math.pow((cC + 0.055) / 1.055, 2.4)

                when {
                    r == null -> r = cC
                    g == null -> g = cC
                    else -> b = cC
                }
            }

            val L = 0.2126 * r!! + 0.7152 * g!! + 0.0722 * b!!


            return if (L > 0.179) Color.BLACK else Color.WHITE
        }

        fun getThemeFromColor(color: String?): Int {

            if (color == null)
                return -1

            when (color) {
                "#ffcdd2" -> return R.style.colorPrimary_Pink
                "#e57373" -> return R.style.colorPrimary_e57
                "#d32f2f" -> return R.style.colorPrimary_d32
                "#f06292" -> return R.style.colorPrimary_f06
                "#c2185b" -> return R.style.colorPrimary_c21
                "#880e4f" -> return R.style.colorPrimary_880
                "#f50057" -> return R.style.colorPrimary_f50
                "#e1bee7" -> return R.style.colorPrimary_e1b
                "#ba68c8" -> return R.style.colorPrimary_ba6
                "#8e24aa" -> return R.style.colorPrimary_8e2
                "#aa00ff" -> return R.style.colorPrimary_aa0
                "#7e57c2" -> return R.style.colorPrimary_7e5
                "#4527a0" -> return R.style.colorPrimary_452
                "#7c4dff" -> return R.style.colorPrimary_7c4
                "#6200ea" -> return R.style.colorPrimary_620
                "#c5cae9" -> return R.style.colorPrimary_c5c
                "#7986cb" -> return R.style.colorPrimary_798
                "#3949ab" -> return R.style.colorPrimary_394
                "#304ffe" -> return R.style.colorPrimary_304
                "#e3f2fd" -> return R.style.colorPrimary_e3f
                "#64b5f6" -> return R.style.colorPrimary_64b
                "#1976d2" -> return R.style.colorPrimary_197
                "#0288d1" -> return R.style.colorPrimary_028
                "#40c4ff" -> return R.style.colorPrimary_40c
                "#00b0ff" -> return R.style.colorPrimary_00b
                "#80deea" -> return R.style.colorPrimary_80d
                "#00acc1" -> return R.style.colorPrimary_00a
                "#00838f" -> return R.style.colorPrimary_008
                "#006064" -> return R.style.colorPrimary_006
                "#e0f2f1" -> return R.style.colorPrimary_e0f
                "#80cbc4" -> return R.style.colorPrimary_80c
                "#00695c" -> return R.style.colorPrimary_0069
                "#64ffda" -> return R.style.colorPrimary_64f
                "#c8e6c9" -> return R.style.colorPrimary_c8e
                "#66bb6a" -> return R.style.colorPrimary_66b
                "#2e7d32" -> return R.style.colorPrimary_2e7
                "#60ad5e" -> return R.style.colorPrimary_60a
                "#00e676" -> return R.style.colorPrimary_00e
                "#aed581" -> return R.style.colorPrimary_aed
                "#689f38" -> return R.style.colorPrimary_689
                "#33691e" -> return R.style.colorPrimary_336
                "#76ff03" -> return R.style.colorPrimary_76f
                "#64dd17" -> return R.style.colorPrimary_64d
                "#cddc39" -> return R.style.colorPrimary_cdd
                "#9e9d24" -> return R.style.colorPrimary_9e9
                "#827717" -> return R.style.colorPrimary_827
                "#fff9c4" -> return R.style.colorPrimary_fff
                "#fbc02d" -> return R.style.colorPrimary_fbc
                "#f57f17" -> return R.style.colorPrimary_f57
                "#ffff00" -> return R.style.colorPrimary_ffff
                "#ffcc80" -> return R.style.colorPrimary_ffc
                "#ffccbc" -> return R.style.colorPrimary_ffcc
                "#ffab91" -> return R.style.colorPrimary_ffa
                "#bcaaa4" -> return R.style.colorPrimary_bca
                "#8d6e63" -> return R.style.colorPrimary_8d6
                "#4e342e" -> return R.style.colorPrimary_4e3
                "#fafafa" -> return R.style.colorPrimary_faf
                "#bdbdbd" -> return R.style.colorPrimary_bdb
                "#757575" -> return R.style.colorPrimary_757
                "#424242" -> return R.style.colorPrimary_424
                "#cfd8dc" -> return R.style.colorPrimary_cfd
                "#b0bec5" -> return R.style.colorPrimary_b0b
                "#607d8b" -> return R.style.colorPrimary_607
                "#37474f" -> return R.style.colorPrimary_374
                else -> return -1
            }
        }

        fun getPrimaryColor(context: Context, colorType: ColorType): Int {

            val id: Int = when (colorType) {
                ColorUtils.ColorType.ACCENT -> R.attr.colorAccent
                ColorUtils.ColorType.PRIMARY_DARK -> R.attr.colorPrimaryDark
                ColorUtils.ColorType.PRIMARY_LIGHT -> R.attr.colorPrimaryLight
                ColorUtils.ColorType.PRIMARY -> R.attr.colorPrimary
                else -> R.attr.colorPrimary
            }

            val typedValue = TypedValue()
            val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(id))
            val colorToReturn = a.getColor(0, 0)
            a.recycle()
            return colorToReturn
        }
    }

    public enum class ColorType {
        PRIMARY, PRIMARY_LIGHT, PRIMARY_DARK, ACCENT
    }
}