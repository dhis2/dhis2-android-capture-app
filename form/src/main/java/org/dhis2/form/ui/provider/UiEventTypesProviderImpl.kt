package org.dhis2.form.ui.provider

import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.program.SectionRenderingType

class UiEventTypesProviderImpl : UiEventTypesProvider {

    override fun provideUiRenderType(
        featureType: FeatureType?,
        valueTypeRenderingType: ValueTypeRenderingType?,
        sectionRenderingType: SectionRenderingType?
    ): UiRenderType {
        return when (sectionRenderingType) {
            SectionRenderingType.SEQUENTIAL -> UiRenderType.SEQUENCIAL
            SectionRenderingType.MATRIX -> UiRenderType.MATRIX
            else -> when (valueTypeRenderingType) {
                ValueTypeRenderingType.VERTICAL_RADIOBUTTONS ->
                    UiRenderType.VERTICAL_RADIOBUTTONS
                ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS ->
                    UiRenderType.HORIZONTAL_RADIOBUTTONS
                ValueTypeRenderingType.VERTICAL_CHECKBOXES ->
                    UiRenderType.VERTICAL_CHECKBOXES
                ValueTypeRenderingType.HORIZONTAL_CHECKBOXES ->
                    UiRenderType.HORIZONTAL_CHECKBOXES
                ValueTypeRenderingType.AUTOCOMPLETE ->
                    UiRenderType.AUTOCOMPLETE
                ValueTypeRenderingType.QR_CODE ->
                    UiRenderType.QR_CODE
                ValueTypeRenderingType.BAR_CODE ->
                    UiRenderType.BAR_CODE
                ValueTypeRenderingType.CANVAS ->
                    UiRenderType.CANVAS
                else -> when (featureType) {
                    FeatureType.POINT -> UiRenderType.POINT
                    FeatureType.POLYGON -> UiRenderType.POLYGON
                    FeatureType.MULTI_POLYGON -> UiRenderType.MULTI_POLYGON
                    else -> UiRenderType.DEFAULT
                }
            }
        }
    }
}
