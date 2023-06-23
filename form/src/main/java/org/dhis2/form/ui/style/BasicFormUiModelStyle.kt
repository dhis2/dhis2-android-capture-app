package org.dhis2.form.ui.style

import org.dhis2.form.R
import org.hisp.dhis.android.core.common.ValueType

data class BasicFormUiModelStyle(
    val factory: FormUiColorFactory,
    val valueType: ValueType,
    val actionIconClickable: Boolean
) : FormUiModelStyle {
    private var colors = factory.getBasicColors()

    override fun getColors(): Map<FormUiColorType, Int> {
        return colors
    }

    override fun getDescriptionIcon(): Int? = when (valueType) {
        ValueType.DATE -> R.drawable.ic_form_date
        ValueType.DATETIME -> R.drawable.ic_form_date_time
        ValueType.TIME -> R.drawable.ic_form_time
        ValueType.PERCENTAGE -> R.drawable.ic_form_percentage
        ValueType.EMAIL -> R.drawable.ic_form_email
        ValueType.PHONE_NUMBER -> R.drawable.ic_form_phone
        else -> null
    }

    override fun isActionIconClickable(): Boolean {
        return when (valueType) {
            ValueType.DATE,
            ValueType.DATETIME,
            ValueType.TIME -> true
            ValueType.PERCENTAGE -> false
            ValueType.EMAIL,
            ValueType.PHONE_NUMBER -> actionIconClickable
            else -> false
        }
    }

    override fun textColor(error: String?, warning: String?): Int? {
        val colorType = when {
            warning != null -> FormUiColorType.WARNING
            error != null -> FormUiColorType.ERROR
            else -> FormUiColorType.TEXT_PRIMARY
        }
        return colors[colorType]
    }

    override fun backgroundColor(
        valueType: ValueType,
        error: String?,
        warning: String?
    ): Pair<Array<Int>, Int?> {
        val colorType = when {
            warning != null -> FormUiColorType.WARNING
            error != null -> FormUiColorType.ERROR
            else -> FormUiColorType.TEXT_PRIMARY
        }
        return Pair(emptyArray(), colors[colorType])
    }
}
