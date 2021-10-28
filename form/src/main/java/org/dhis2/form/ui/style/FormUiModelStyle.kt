package org.dhis2.form.ui.style

import org.hisp.dhis.android.core.common.ValueType

interface FormUiModelStyle {
    fun getColors(): Map<FormUiColorType, Int>
    fun getDescriptionIcon(): Int
    fun backgroundColor(
        valueType: ValueType,
        error: String?,
        warning: String?
    ): Pair<Array<Int>, Int>
}
