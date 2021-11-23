package org.dhis2.data.forms.dataentry.fields.age

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import java.util.Calendar
import java.util.Date
import org.dhis2.Bindings.toDate
import org.dhis2.form.ui.binding.setInputStyle
import org.dhis2.utils.DateUtils

@BindingAdapter("onFocusChangeAgeView")
fun onFocusChangesAgeView(editText: EditText, model: AgeViewModel) {
    editText.setOnFocusChangeListener { v, hasFocus -> editText.setInputStyle(model) }
}

@BindingAdapter("warning", "error", "isSearchMode")
fun setWarningOrError(textView: TextView, warning: String?, error: String?, isSearchMode: Boolean) {
    if (warning != null) {
        val color = ContextCompat.getColor(textView.context, org.dhis2.R.color.warning_color)
        textView.setTextColor(color)
        textView.visibility = View.VISIBLE
        textView.text = warning
    } else if (error != null) {
        val color = ContextCompat.getColor(textView.context, org.dhis2.R.color.error_color)
        textView.setTextColor(color)
        textView.visibility = View.VISIBLE
        textView.text = error
    } else if (!isSearchMode) {
        val color = ContextCompat.getColor(textView.context, org.dhis2.R.color.textPrimary)
        textView.setTextColor(color)
        textView.visibility = View.GONE
    }
}

@BindingAdapter(value = ["setInitialValueDate", "parsingErrorText"], requireAll = true)
fun EditText.setInitialValueDate(value: String?, errorTextView: TextView) {
    if (value.isNullOrEmpty()) {
        text = null
    } else {
        try {
            val initialDate = value.toDate()
            val dateFormat = DateUtils.uiDateFormat()
            val result = dateFormat.format(initialDate)
            Calendar.getInstance().time = initialDate
            setText(result)
            errorTextView.visibility = View.GONE
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
    } else try {
        val dateDifference = getDifferenceBetweenDates(value)
        editText.setText(dateDifference[0].toString())
    } catch (e: Exception) {
        editText.text = null
    }
}

@BindingAdapter("setInitialValueMonth")
fun setInitialValueMonth(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else try {
        val dateDifference = getDifferenceBetweenDates(value)
        editText.setText(dateDifference[1].toString())
    } catch (e: Exception) {
        editText.text = null
    }
}

@BindingAdapter("setInitialValueDay")
fun setInitialValueDay(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else try {
        val dateDifference = getDifferenceBetweenDates(value)
        editText.setText(dateDifference[2].toString())
    } catch (e: Exception) {
        editText.text = null
    }
}

fun getDifferenceBetweenDates(value: String?): IntArray {
    val initialDate: Date = value!!.toDate()
    Calendar.getInstance().time = initialDate
    return DateUtils.getDifference(
        initialDate,
        Calendar.getInstance().time
    )
}

fun negativeOrZero(value: String): Int {
    return if (value.isEmpty()) 0 else -Integer.valueOf(value)
}
