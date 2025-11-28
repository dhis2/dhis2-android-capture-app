package org.dhis2.commons.ui.extensions

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import org.dhis2.commons.R
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

fun Activity.handleInsets() {
    val rootView: View = window.decorView.findViewById(android.R.id.content)
    rootView.background = this.getBackgroundGradientDrawable()

    ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
        val bars =
            insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
                    or WindowInsetsCompat.Type.ime(),
            )
        v.updatePadding(
            left = bars.left,
            top = bars.top,
            right = bars.right,
            bottom = bars.bottom,
        )
        WindowInsetsCompat.CONSUMED
    }
}

private fun Activity.getBackgroundGradientDrawable(): GradientDrawable {
    val primary = getColorPrimary()
    val white = SurfaceColor.SurfaceBright.toArgb()
    return GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(
            primary,
            primary,
            white,
            white,
        ),
    )
}

private fun Context.getColorPrimary(): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}
