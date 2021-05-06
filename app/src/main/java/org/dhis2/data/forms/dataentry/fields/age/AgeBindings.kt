package org.dhis2.data.forms.dataentry.fields.age

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
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
    val color = if (!isBgTransparent){
        ColorUtils.getPrimaryColor(imageView.context, ColorUtils.ColorType.ACCENT)
    } else {
        ColorUtils.getPrimaryColor(imageView.context, ColorUtils.ColorType.PRIMARY)
    }
    imageView.setColorFilter(color)
}

/*@BindingAdapter({"bind:color"})
fun setColor(textView :  , primaryTextColor :String ) {
    textView.textColor = (ContextCompat.getColor(textView.getContext(), primaryTextColor)
} */

@BindingConversion
fun convertColorToDrawable(color: Int) = ColorDrawable(color)