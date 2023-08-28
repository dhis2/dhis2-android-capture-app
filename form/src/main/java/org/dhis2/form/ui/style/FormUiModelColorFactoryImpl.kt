package org.dhis2.form.ui.style

import android.content.Context
import androidx.core.content.ContextCompat
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.form.R

class FormUiModelColorFactoryImpl(
    val context: Context,
    val isBackgroundTransparent: Boolean = false,
    val colorUtils: ColorUtils,
) : FormUiColorFactory {
    override fun getBasicColors(): Map<FormUiColorType, Int> {
        if (isBackgroundTransparent) {
            return mapOf(
                FormUiColorType.PRIMARY to
                    colorUtils.getPrimaryColor(context, ColorType.PRIMARY),
                FormUiColorType.TEXT_PRIMARY to
                    ContextCompat.getColor(context, R.color.textPrimary),
                FormUiColorType.FIELD_LABEL_TEXT to
                    ContextCompat.getColor(context, R.color.text_black_A63),
                FormUiColorType.WARNING to
                    ContextCompat.getColor(context, R.color.warning_color),
                FormUiColorType.ERROR to
                    ContextCompat.getColor(context, R.color.error_color),
                FormUiColorType.ACTION_ICON to
                    ContextCompat.getColor(context, R.color.colorGreyDefault),
            )
        }
        return mapOf(
            FormUiColorType.PRIMARY to
                colorUtils.getPrimaryColor(context, ColorType.ACCENT),
            FormUiColorType.TEXT_PRIMARY to
                colorUtils.getPrimaryColor(context, ColorType.ACCENT),
            FormUiColorType.FIELD_LABEL_TEXT to
                colorUtils.getPrimaryColor(context, ColorType.ACCENT),
            FormUiColorType.WARNING to
                ContextCompat.getColor(context, R.color.warning_color),
            FormUiColorType.ERROR to
                ContextCompat.getColor(context, R.color.error_color),
            FormUiColorType.ACTION_ICON to
                colorUtils.getPrimaryColor(context, ColorType.ACCENT),
        )
    }
}
