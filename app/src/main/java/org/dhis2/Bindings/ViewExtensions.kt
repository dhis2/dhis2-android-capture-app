package org.dhis2.Bindings

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.closeKeyboard() {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
