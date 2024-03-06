package org.dhis2.form.ui.style

import org.hisp.dhis.android.core.common.ValueType

class LongTextDecorator(
    val style: FormUiModelStyle,
    private val longTextFactory: FormUiColorFactory
) :
    FormUiModelStyle {
    override fun getColors(): Map<FormUiColorType, Int> {
        return longTextFactory.getBasicColors()
    }

    override fun getDescriptionIcon(): Int? {
        return style.getDescriptionIcon()
    }

    override fun isActionIconClickable(): Boolean {
        return style.isActionIconClickable()
    }

    override fun textColor(error: String?, warning: String?): Int? {
        val colorType = when {
            warning != null -> FormUiColorType.WARNING
            error != null -> FormUiColorType.ERROR
            else -> FormUiColorType.TEXT_PRIMARY
        }
        return getColors()[colorType]
    }

    override fun backgroundColor(
        valueType: ValueType,
        error: String?,
        warning: String?
    ): Pair<Array<Int>, Int?> {
        return style.backgroundColor(
            valueType,
            error,
            warning
        )
    }
}
