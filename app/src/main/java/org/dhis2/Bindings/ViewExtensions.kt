package org.dhis2.Bindings

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
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
