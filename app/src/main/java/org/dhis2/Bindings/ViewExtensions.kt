package org.dhis2.Bindings

import android.app.Activity
import android.graphics.Outline
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.tbuonomo.viewpagerdotsindicator.R

@Deprecated("Use org.dhis2.commons.extensions.closeKeyboard instead")
fun View.closeKeyboard() {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

@Deprecated("Use org.dhis2.commons.extensions.openKeyboard instead")
fun View.openKeyboard() {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

fun View.getThemePrimaryColor(): Int {
    val value = TypedValue()
    context.theme.resolveAttribute(R.attr.colorPrimary, value, true)
    return value.data
}

fun View.onFocusRemoved(onFocusRemovedCallback: () -> Unit) {
    setOnFocusChangeListener { view, hasFocus ->
        if (!hasFocus) {
            view.closeKeyboard()
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
