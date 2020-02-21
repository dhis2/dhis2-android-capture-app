package org.dhis2.Bindings

import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import org.dhis2.R

fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}

fun TextInputEditText.placeHolder(placeholder: String) {
    this.setHintTextColor(ContextCompat.getColor(context, R.color.placeholder))
    this.hint = placeholder

    this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            this.hint = ""
        }
    }
}

fun TextView.setDateInterval(string: String?) {
    text = string?.toDateSpan(context)
}
