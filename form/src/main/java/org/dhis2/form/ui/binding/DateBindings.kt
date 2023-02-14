package org.dhis2.form.ui.binding

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.databinding.BindingAdapter
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType

@BindingAdapter("requestDateFocus")
fun bindRequestDateFocus(editText: EditText, item: FieldUiModel?) {
    item?.let {
        if (it.focused) {
            editText.requestFocus()
            editText.closeKeyboard()
        } else {
            editText.clearFocus()
        }
        editText.isCursorVisible = false
    }
}

@SuppressLint("ClickableViewAccessibility")
@BindingAdapter("setOnDateTouchListener")
fun bindOnDateTouchListener(editText: EditText, item: FieldUiModel?) {
    editText.setOnTouchListener { _: View?, event: MotionEvent ->
        if (MotionEvent.ACTION_UP == event.action && item?.focused != true) {
            item?.invokeUiEvent(UiEventType.DATE_TIME)
        }
        false
    }
}
