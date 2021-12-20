package org.dhis2.form.ui.provider

import kotlin.reflect.KClass
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType

interface LayoutProvider {
    fun getLayoutByModel(modelClass: KClass<*>): Int
    fun getLayoutByType(
        valueType: ValueType?,
        renderingType: ValueTypeRenderingType?,
        sectionRenderingType: ProgramStageSectionRenderingType?
    ): Int
    fun getLayoutForSection(): Int
}
