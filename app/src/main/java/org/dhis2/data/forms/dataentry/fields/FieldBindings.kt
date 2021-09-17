package org.dhis2.data.forms.dataentry.fields

import android.content.res.ColorStateList
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.Bindings.toDate
import org.dhis2.Bindings.toTime
import org.dhis2.R
import org.dhis2.form.ui.style.FormUiColorType
import org.dhis2.form.ui.style.FormUiModelStyle
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.common.ValueType

@BindingAdapter("label_text_color")
fun TextView.setLabelTextColor(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.FIELD_LABEL_TEXT]?.let { color ->
            setTextColor(color)
        }
    }
}

@BindingAdapter("icon_color")
fun ImageView.setIconColor(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.FIELD_LABEL_TEXT]?.let { color ->
            setColorFilter(color)
        }
    }
}

@BindingAdapter("description_icon_tint")
fun ImageView.tintDescriptionIcon(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.PRIMARY]?.let { color ->
            setColorFilter(color)
        }
    }
}

@BindingAdapter("input_style")
fun EditText.setInputStyle(styleItem: FieldViewModel?) {
    styleItem?.style()?.let { style ->
        style.getColors()[FormUiColorType.TEXT_PRIMARY]?.let { color ->
            setTextColor(color)
        }
        val colorType = when {
            styleItem.warning() != null -> FormUiColorType.WARNING
            styleItem.error() != null -> FormUiColorType.ERROR
            else -> FormUiColorType.TEXT_PRIMARY
        }
        style.getColors()[colorType]?.let { color ->
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(color))
        }
    }
}

@BindingAdapter("input_layout_style")
fun TextInputLayout.setInputLayoutStyle(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.FIELD_LABEL_TEXT]?.let { color ->
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_focused),
                    intArrayOf(-android.R.attr.state_focused)
                ),
                intArrayOf(
                    color,
                    color
                )
            )
            defaultHintTextColor = colorStateList
            boxBackgroundColor = color
        }
    }
}

@BindingAdapter("initialValue", "valueType")
fun TextInputEditText.setInitialValue(value: String?, valueType: ValueType) {
    try {
        var formattedValue = value
        when (valueType) {
            ValueType.DATE -> formattedValue = value?.toDate()?.let {
                DateUtils.uiDateFormat().format(it)
            }
            ValueType.DATETIME -> formattedValue = value?.toDate()?.let {
                DateUtils.dateTimeFormat().format(it)
            }
            ValueType.TIME -> formattedValue = value?.toTime()?.let {
                when {
                    DateFormat.is24HourFormat(context) -> DateUtils.timeFormat().format(it)
                    else -> DateUtils.twelveHourTimeFormat().format(it)
                }
            }
            else -> {}
        }
        setText(formattedValue)
    } catch (e: Exception) {
        error = e.message
    }
}

@BindingAdapter("inputWarning", "inputError")
fun TextInputLayout.setWarningErrorMessage(warning: String?, error: String?) {
    when {
        error != null -> {
            setErrorTextAppearance(R.style.error_appearance)
            this.error = error
            editText?.text = null
        }
        warning != null -> {
            setErrorTextAppearance(R.style.warning_appearance)
            this.error = warning
        }
        else -> this.error = null
    }
}
