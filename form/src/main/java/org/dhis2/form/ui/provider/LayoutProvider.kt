package org.dhis2.form.ui.provider

import kotlin.reflect.KClass
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

interface LayoutProvider {
    fun getLayoutByModel(modelClass: KClass<*>): Int
    fun getLayoutByValueType(valueType: ValueType): Int
    fun getLayoutByValueRenderingType(
        renderingType: ValueTypeRenderingType,
        valueType: ValueType
    ): Int
}
