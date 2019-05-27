package org.dhis2.Bindings

import android.view.MotionEvent
import android.widget.EditText
import androidx.lifecycle.MutableLiveData

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = this.apply { setValue(initialValue) }

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
