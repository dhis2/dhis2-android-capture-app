package org.dhis2.form.ui.style

import org.hisp.dhis.android.core.common.ValueType

interface FormUiModelStyle {
    fun getColors(): Map<FormUiColorType, Int>
    fun getDescriptionIcon(): Int?
    fun isActionIconClickable(): Boolean
    fun textColor(error: String?, warning: String?): Int?
    fun backgroundColor(
        valueType: ValueType,
        error: String?,
        warning: String?,
    ): Pair<Array<Int>, Int?>
}
