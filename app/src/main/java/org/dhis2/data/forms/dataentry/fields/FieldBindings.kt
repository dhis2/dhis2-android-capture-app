package org.dhis2.data.forms.dataentry.fields

import android.R
import android.content.res.ColorStateList
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
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
                    intArrayOf(R.attr.state_focused),
                    intArrayOf(-R.attr.state_focused)
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
