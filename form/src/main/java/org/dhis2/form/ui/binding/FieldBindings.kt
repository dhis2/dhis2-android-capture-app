package org.dhis2.form.ui.binding

import android.content.res.ColorStateList
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.style.FormUiColorType
import org.dhis2.form.ui.style.FormUiModelStyle

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

@BindingAdapter("action_icon_tint")
fun ImageView.tintActionIcon(style: FormUiModelStyle?) {
    style?.let {
        style.getColors()[FormUiColorType.ACTION_ICON]?.let { color ->
            setColorFilter(color)
        }
    }
}

@BindingAdapter("input_style")
fun TextView.setInputStyle(styleItem: FieldUiModel?) {
    styleItem?.let { uiModel ->
        uiModel.textColor?.let {
            setTextColor(it)
        }
        uiModel.backGroundColor?.let {
            ViewCompat.setBackgroundTintList(
                this,
                ColorStateList.valueOf(it.second)
            )
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
