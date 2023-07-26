package org.dhis2.commons.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.closeKeyboard() {
    post {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun View.openKeyboard() {
    post {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, 0)
    }
}
