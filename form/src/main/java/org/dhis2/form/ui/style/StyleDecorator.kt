package org.dhis2.form.ui.style

import org.hisp.dhis.android.core.common.ValueType

open class StyleDecorator(protected var style: FormUiModelStyle) : FormUiModelStyle {
    override fun getColors(): Map<FormUiColorType, Int> {
        return style.getColors()
    }

    override fun getDescriptionIcon(): Int? {
        return getDescriptionIcon()
    }

    override fun textColor(error: String?, warning: String?): Int? {
        return textColor(error, warning)
    }

    override fun backgroundColor(
        valueType: ValueType,
        error: String?,
        warning: String?
    ): Pair<Array<Int>, Int> {
        return backgroundColor(valueType, error, warning)
    }
}
