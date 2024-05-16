package org.dhis2.commons.bindings

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Outline
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import org.dhis2.commons.R
import kotlin.math.pow

@BindingAdapter("scrollingTextView")
fun TextView.setScrollingTextView(canScroll: Boolean) {
    if (canScroll) {
        movementMethod = ScrollingMovementMethod()
    }
}

@BindingAdapter("progressColor")
fun ProgressBar.setProgressColor(color: Int) {
    val typedValue = TypedValue()
    val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
    val color2 = a.getColor(0, 0)
    a.recycle()
    indeterminateDrawable.setColorFilter(color2, PorterDuff.Mode.SRC_IN)
}

@BindingAdapter("iconResource")
fun ImageView.setIconResource(@DrawableRes iconResource: Int) {
    setImageResource(iconResource)
}

@BindingAdapter("fromResBgColor")
fun setFromResBgColor(view: View?, color: Int) {
    val tintedColor: String
    val rgb = ArrayList<Double>()
    rgb.add(Color.red(color) / 255.0)
    rgb.add(Color.green(color) / 255.0)
    rgb.add(Color.blue(color) / 255.0)
    var r: Double? = null
    var g: Double? = null
    var b: Double? = null
    for (c in rgb) {
        val color = if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        if (r == null) r = color else if (g == null) g = color else b = color
    }
    val L = 0.2126 * r!! + 0.7152 * g!! + 0.0722 * b!!
    tintedColor = if (L > 0.179) {
        "#000000" // bright colors - black font
    } else {
        "#FFFFFF" // dark colors - white font
    }
    if (view is TextView) {
        view.setTextColor(Color.parseColor(tintedColor))
    }
    if (view is ImageView) {
        val drawable = view.drawable
        drawable?.setColorFilter(Color.parseColor(tintedColor), PorterDuff.Mode.SRC_IN)
        view.setImageDrawable(drawable)
    }
}

@BindingAdapter("textStyle")
fun TextView.setTextStyle(style: Int) {
    when (style) {
        Typeface.BOLD -> setTypeface(null, Typeface.BOLD)
        else -> setTypeface(null, Typeface.NORMAL)
    }
}

@BindingAdapter("marginTop")
fun View.setMarginTop(marginInDp: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val p = layoutParams as ViewGroup.MarginLayoutParams
        p.setMargins(p.leftMargin, marginInDp.dp, p.rightMargin, p.bottomMargin)
        requestLayout()
    }
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun View.clipWithRoundedCorners(curvedRadio: Int = 16.dp) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height + curvedRadio,
                    curvedRadio.toFloat(),
                )
            }
        }
        clipToOutline = true
    }
}

fun View.clipWithAllRoundedCorners(curvedRadio: Int = 16.dp) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height,
                    curvedRadio.toFloat(),
                )
            }
        }
        clipToOutline = true
    }
}

fun HorizontalScrollView.scrollToPosition(viewTag: String) {
    val view = findViewWithTag<View>(viewTag) ?: return
    val xScroll = when (context.resources.configuration.layoutDirection) {
        View.LAYOUT_DIRECTION_RTL -> view.right - view.paddingRight
        View.LAYOUT_DIRECTION_LTR -> view.left - view.paddingLeft
        else -> 0
    }
    smoothScrollTo(xScroll, view.top)
}

fun <T> MutableList<T>.addIf(ifCondition: Boolean, itemToAdd: T, index: Int? = null) {
    if (ifCondition) {
        index?.let { add(it, itemToAdd) } ?: add(itemToAdd)
    }
}
