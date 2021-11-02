package org.dhis2.form.ui.style

import org.dhis2.form.R
import org.hisp.dhis.android.core.common.ValueType

data class BasicFormUiModelStyle(
    val factory: FormUiColorFactory,
    val valueType: ValueType
) : FormUiModelStyle {
    private var colors = factory.getBasicColors()

    override fun getColors(): Map<FormUiColorType, Int> {
        return colors
    }

    override fun getDescriptionIcon(): Int =
        when (valueType) {
            ValueType.TEXT -> TODO()
            ValueType.LONG_TEXT -> TODO()
            ValueType.LETTER -> TODO()
            ValueType.BOOLEAN -> TODO()
            ValueType.TRUE_ONLY -> TODO()
            ValueType.DATE -> R.drawable.ic_form_date
            ValueType.DATETIME -> R.drawable.ic_form_date_time
            ValueType.TIME -> R.drawable.ic_form_time
            ValueType.NUMBER -> TODO()
            ValueType.UNIT_INTERVAL -> TODO()
            ValueType.PERCENTAGE -> TODO()
            ValueType.INTEGER -> TODO()
            ValueType.INTEGER_POSITIVE -> TODO()
            ValueType.INTEGER_NEGATIVE -> TODO()
            ValueType.INTEGER_ZERO_OR_POSITIVE -> TODO()
            ValueType.FILE_RESOURCE -> TODO()
            ValueType.COORDINATE -> TODO()
            ValueType.PHONE_NUMBER -> TODO()
            ValueType.EMAIL -> TODO()
            ValueType.USERNAME -> TODO()
            ValueType.ORGANISATION_UNIT -> TODO()
            ValueType.TRACKER_ASSOCIATE -> TODO()
            ValueType.AGE -> TODO()
            ValueType.URL -> TODO()
            ValueType.IMAGE -> TODO()
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
    ): Pair<Array<Int>, Int> {
        val colorType = when {
            warning != null -> FormUiColorType.WARNING
            error != null -> FormUiColorType.ERROR
            else -> FormUiColorType.TEXT_PRIMARY
        }
        return Pair(emptyArray(), colors[colorType]!!)
    }
}
