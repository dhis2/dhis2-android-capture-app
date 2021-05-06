package org.dhis2.data.forms.dataentry.fields.age

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.utils.ColorUtils

@BindingAdapter("setBackgroundColorAgeView")
fun setBackgroundColorAgeView(
    view: View,
    isBgTransparent: Boolean
) {
    if (!isBgTransparent) {
        view.setBackgroundColor(
            ColorUtils.getPrimaryColor(
                view.context,
                ColorUtils.ColorType.ACCENT
            )
        )
    }
}

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


@BindingAdapter("setTextColorHintAgeVuew")
fun setTextColorHintAgeView(textInputLayout: TextInputLayout, isBgTransparent: Boolean){
    if (!isBgTransparent) {
        textInputLayout.defaultHintTextColor
    }
}

