package org.dhis2.form.ui.style

import android.content.Context
import androidx.core.content.ContextCompat
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.form.R

class LongTextUiColorFactoryImpl(val context: Context, val isBackgroundTransparent: Boolean) :
    FormUiColorFactory {
    override fun getBasicColors(): Map<FormUiColorType, Int> {
        if (isBackgroundTransparent) {
            return mapOf(
                FormUiColorType.PRIMARY to
                    ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY),
                FormUiColorType.TEXT_PRIMARY to
                    ContextCompat.getColor(context, R.color.textPrimary),
                FormUiColorType.FIELD_LABEL_TEXT to
                    ContextCompat.getColor(context, R.color.text_black_A63),
                FormUiColorType.WARNING to
                    ContextCompat.getColor(context, R.color.warning_color),
                FormUiColorType.ERROR to
                    ContextCompat.getColor(context, R.color.error_color),
                FormUiColorType.ACTION_ICON to
                    ContextCompat.getColor(context, R.color.colorGreyDefault)
            )
        }
        return mapOf(
            FormUiColorType.PRIMARY to
                ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY),
            FormUiColorType.TEXT_PRIMARY to
                ContextCompat.getColor(context, R.color.textPrimary),
            FormUiColorType.FIELD_LABEL_TEXT to
                ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY),
            FormUiColorType.WARNING to
                ContextCompat.getColor(context, R.color.warning_color),
            FormUiColorType.ERROR to
                ContextCompat.getColor(context, R.color.error_color),
            FormUiColorType.ACTION_ICON to
                ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.ACCENT)
        )
    }
}
