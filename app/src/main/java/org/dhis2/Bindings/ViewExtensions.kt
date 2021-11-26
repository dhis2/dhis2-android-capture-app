package org.dhis2.Bindings

import android.graphics.Outline
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.tbuonomo.viewpagerdotsindicator.R
import org.dhis2.commons.extensions.closeKeyboard

fun View.getThemePrimaryColor(): Int {
    val value = TypedValue()
    context.theme.resolveAttribute(R.attr.colorPrimary, value, true)
    return value.data
}

fun View.onFocusRemoved(onFocusRemovedCallback: () -> Unit) {
    setOnFocusChangeListener { view, hasFocus ->
        if (!hasFocus) {
            closeKeyboard()
            onFocusRemovedCallback.invoke()
        }
    }
}

fun TextView.clearFocusOnDone() {
    setOnEditorActionListener { view, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            view.clearFocus()
            true
        } else {
            false
        }
    }
}

fun View.clipWithRoundedCorners(curvedRadio: Int = 16.dp) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height + curvedRadio,
                    curvedRadio.toFloat()
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
                    curvedRadio.toFloat()
                )
            }
        }
        clipToOutline = true
    }
}
