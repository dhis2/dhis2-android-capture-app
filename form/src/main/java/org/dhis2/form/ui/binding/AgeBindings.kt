package org.dhis2.form.ui.binding

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.util.Calendar
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.extensions.toDate

@BindingAdapter(value = ["setInitialValueDate", "parsingErrorText"], requireAll = true)
fun EditText.setInitialValueDate(value: String?, errorTextView: TextView) {
    if (value.isNullOrEmpty()) {
        text = null
    } else {
        try {
            value.toDate()?.let {
                val dateFormat = DateUtils.uiDateFormat()
                val result = dateFormat.format(it)
                Calendar.getInstance().time = it
                setText(result)
                errorTextView.visibility = View.GONE
            }
        } catch (e: Exception) {
            errorTextView.text = errorTextView.text.toString().format(value)
            errorTextView.visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("setInitialValueYear")
fun setInitialValueYear(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else {
        try {
            val dateDifference = getDifferenceBetweenDates(value)
            editText.setText(dateDifference[0].toString())
        } catch (e: Exception) {
            editText.text = null
        }
    }
}

@BindingAdapter("setInitialValueMonth")
fun setInitialValueMonth(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else {
        try {
            val dateDifference = getDifferenceBetweenDates(value)
            editText.setText(dateDifference[1].toString())
        } catch (e: Exception) {
            editText.text = null
        }
    }
}

@BindingAdapter("setInitialValueDay")
fun setInitialValueDay(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else {
        try {
            val dateDifference = getDifferenceBetweenDates(value)
            editText.setText(dateDifference[2].toString())
        } catch (e: Exception) {
            editText.text = null
        }
    }
}

private fun getDifferenceBetweenDates(value: String?): IntArray {
    return value?.toDate()?.let {
        Calendar.getInstance().time = it
        DateUtils.getDifference(
            it,
            Calendar.getInstance().time
        )
    } ?: IntArray(0)
}
