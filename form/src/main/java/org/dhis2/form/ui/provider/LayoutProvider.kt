package org.dhis2.form.ui.provider

import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.program.SectionRenderingType

interface LayoutProvider {
    fun getLayoutByType(
        valueType: ValueType?,
        renderingType: ValueTypeRenderingType?,
        optionSet: String?,
        sectionRenderingType: SectionRenderingType?,
    ): Int
    fun getLayoutForSection(): Int
}
