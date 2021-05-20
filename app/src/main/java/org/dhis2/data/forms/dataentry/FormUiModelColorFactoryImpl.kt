package org.dhis2.data.forms.dataentry

import android.content.Context
import androidx.core.content.ContextCompat
import org.dhis2.form.ui.style.FormUiColorFactory
import org.dhis2.form.ui.style.FormUiColorType
import org.dhis2.utils.ColorUtils

class FormUiModelColorFactoryImpl(
    val context: Context,
    val isBackgroundTransparent: Boolean = false
) : FormUiColorFactory {
    override fun getBasicColors(): Map<FormUiColorType, Int> {
        if (isBackgroundTransparent) {
            return mapOf(
                FormUiColorType.PRIMARY to
                    ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY),
                FormUiColorType.TEXT_PRIMARY to
                    ContextCompat.getColor(context, org.dhis2.R.color.textPrimary),
                FormUiColorType.FIELD_LABEL_TEXT to
                    ContextCompat.getColor(context, org.dhis2.R.color.text_black_A63),
                FormUiColorType.WARNING to
                    ContextCompat.getColor(context, org.dhis2.R.color.warning_color),
                FormUiColorType.ERROR to
                    ContextCompat.getColor(context, org.dhis2.R.color.error_color)
            )
        }
        return mapOf(
            FormUiColorType.PRIMARY to
                ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.ACCENT),
            FormUiColorType.TEXT_PRIMARY to
                ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.ACCENT),
            FormUiColorType.FIELD_LABEL_TEXT to
                ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.ACCENT),
            FormUiColorType.WARNING to
                ContextCompat.getColor(context, org.dhis2.R.color.warning_color),
            FormUiColorType.ERROR to
                ContextCompat.getColor(context, org.dhis2.R.color.error_color)
        )
    }
}
