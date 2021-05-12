package org.dhis2.data.forms.dataentry.fields.age

import android.R
import android.content.res.ColorStateList
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.Date
import org.dhis2.Bindings.toDate
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.DateUtils

@BindingAdapter("setTextColorAgeView")
fun setTextColorAgeView(
    textView: TextView,
    isBgTransparent: Boolean
) {
    if (!isBgTransparent) {
        textView.setTextColor(
            ColorUtils.getPrimaryColor(
                textView.context,
                ColorUtils.ColorType.ACCENT
            )
        )
    }
}

@BindingAdapter("tintDescriptionLabel")
fun tintDescriptionLabel(
    imageView: ImageView,
    isBgTransparent: Boolean
) {
    val color = if (!isBgTransparent) {
        ColorUtils.getPrimaryColor(imageView.context, ColorUtils.ColorType.ACCENT)
    } else {
        ColorUtils.getPrimaryColor(imageView.context, ColorUtils.ColorType.PRIMARY)
    }
    imageView.setColorFilter(color)
}

@BindingAdapter("setEditTextColor")
fun setEditTextColor(
    editText: EditText,
    isBgTransparent: Boolean
) {
    if (!isBgTransparent) {
        editText.setTextColor(
            ColorUtils.getPrimaryColor(
                editText.context,
                ColorUtils.ColorType.ACCENT
            )
        )
    } else {
        editText.setTextColor(
            ContextCompat.getColor(editText.context, org.dhis2.R.color.textPrimary)
        )
    }
}

@BindingAdapter("setUnderlineColorEditText")
fun setEditTextUnderlineColor(
    editText: EditText,
    model: AgeViewModel
) {
    val color = if (!model.isBackgroundTransparent) {
        ColorUtils.getPrimaryColor(editText.context, ColorUtils.ColorType.ACCENT)
    } else if (model.warning() != null) {
        ContextCompat.getColor(editText.context, org.dhis2.R.color.warning_color)
    } else if (model.error() != null) {
        ContextCompat.getColor(editText.context, org.dhis2.R.color.error_color)
    } else {
        ContextCompat.getColor(editText.context, org.dhis2.R.color.textPrimary)
    }

    ViewCompat.setBackgroundTintList(editText, ColorStateList.valueOf(color))
}

@BindingAdapter("setTextColorHint")
fun setTextColorHintTextInputLayout(
    textInputLayout: TextInputLayout,
    isBgTransparent: Boolean
) {
    if (!isBgTransparent) {
        val color = ColorUtils.getPrimaryColor(
            textInputLayout.context,
            ColorUtils.ColorType.ACCENT
        )
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(R.attr.state_focused),
                intArrayOf(-R.attr.state_focused)
            ), intArrayOf(
                color, color
            )
        )
        textInputLayout.apply {
            defaultHintTextColor = colorStateList
            boxBackgroundColor = color
        }
    }
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

@BindingAdapter("setInitialValueDate")
fun setInitialValueDate(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else {
        val initialDate = value.toDate()
        val dateFormat = DateUtils.uiDateFormat()
        val result = dateFormat.format(initialDate)
        Calendar.getInstance().time = initialDate
        editText.setText(result)
    }
}

@BindingAdapter("setInitialValueYear")
fun setInitialValueYear(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else {
        val dateDifference = getDifferenceBetweenDates(value)
        editText.setText(dateDifference[0].toString())
    }
}

@BindingAdapter("setInitialValueMonth")
fun setInitialValueMonth(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else {
        val dateDifference = getDifferenceBetweenDates(value)
        editText.setText(dateDifference[1].toString())
    }
}

@BindingAdapter("setInitialValueDay")
fun setInitialValueDay(editText: EditText, value: String?) {
    if (value.isNullOrEmpty()) {
        editText.text = null
    } else {
        val dateDifference = getDifferenceBetweenDates(value)
        editText.setText(dateDifference[2].toString())
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
