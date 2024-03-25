package org.dhis2.form.ui.binding

import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.form.databinding.DataElementLegendBinding
import org.dhis2.form.model.KeyboardActionType
import org.dhis2.form.model.LegendValue

@BindingAdapter("setImeOption")
fun setImeOption(editText: EditText, type: KeyboardActionType?) {
    if (type != null) {
        when (type) {
            KeyboardActionType.NEXT -> editText.imeOptions = IME_ACTION_NEXT
            KeyboardActionType.DONE -> editText.imeOptions = IME_ACTION_DONE
            KeyboardActionType.ENTER -> editText.imeOptions = IME_FLAG_NO_ENTER_ACTION
        }
    }
}

@BindingAdapter("legendBadge")
fun setLegendBadge(legendLayout: FrameLayout, legendValue: LegendValue?) {
    legendLayout.visibility = if (legendValue != null) View.VISIBLE else View.GONE
    if (legendValue != null) {
        val legendBinding: DataElementLegendBinding = DataElementLegendBinding.inflate(
            LayoutInflater.from(legendLayout.context),
        )
        legendBinding.legend = legendValue
        legendLayout.removeAllViews()
        legendLayout.addView(legendBinding.root)
    }
}

@BindingAdapter("legendValue")
fun TextView.setLegend(legendValue: LegendValue?) {
    legendValue?.let {
        DrawableCompat.setTint(background, ColorUtils().withAlpha(it.color, 38))
        compoundDrawables
            .filterNotNull()
            .forEach { drawable -> DrawableCompat.setTint(drawable, it.color) }
    }
}
