package org.dhis2.form.ui

import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import kotlin.reflect.KClass

interface LayoutProvider {
    fun getLayoutByModel(modelClass: KClass<*>): Int?
    fun getLayoutByValueType(valueType: ValueType): Int
    fun getLayoutByValueRenderingType(
        renderingType: ValueTypeRenderingType,
        valueType: ValueType
    ): Int
}