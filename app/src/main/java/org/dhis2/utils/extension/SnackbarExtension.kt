package org.dhis2.utils.extension

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar

fun Snackbar.setIcon(
    drawable: Drawable,
    @ColorInt colorTint: Int = Color.WHITE,
    action: () -> Unit,
): Snackbar =
    this.apply {
        setAction(" ") { action() }
        val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
        textView.text = ""

        drawable.setTint(colorTint)
        drawable.setTintMode(PorterDuff.Mode.SRC_ATOP)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }
