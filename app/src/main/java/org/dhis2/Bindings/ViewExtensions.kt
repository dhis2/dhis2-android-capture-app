package org.dhis2.Bindings

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.tbuonomo.viewpagerdotsindicator.R

fun View.closeKeyboard() {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
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
