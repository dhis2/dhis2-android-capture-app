package org.dhis2.data.forms.dataentry.fields.age

import android.R
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.utils.ColorUtils

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
    isBgTransparent: Boolean
) {
    if (!isBgTransparent) {
        val color = ColorUtils.getPrimaryColor(
            editText.context,
            ColorUtils.ColorType.ACCENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val colorStateList = ColorStateList.valueOf(color)

            editText.backgroundTintList = colorStateList
        } else {
            val drawable: Drawable = editText.background
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            editText.background = drawable
        }
    }
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
                color,
                color
            )
        )
        textInputLayout.apply {
            defaultHintTextColor = colorStateList
            boxBackgroundColor = color
        }
    }
}

@BindingConversion
fun convertColorToDrawable(color: Int) = ColorDrawable(color)