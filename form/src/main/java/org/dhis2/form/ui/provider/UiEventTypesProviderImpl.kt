package org.dhis2.form.ui.provider

import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType

class UiEventTypesProviderImpl : UiEventTypesProvider {

    override fun provideUiRenderType(
        featureType: FeatureType?,
        valueTypeRenderingType: ValueTypeRenderingType?,
        sectionRenderingType: ProgramStageSectionRenderingType?
    ): UiRenderType {
        return when (featureType) {
            FeatureType.POINT -> UiRenderType.POINT
            FeatureType.POLYGON -> UiRenderType.POLYGON
            FeatureType.MULTI_POLYGON -> UiRenderType.MULTI_POLYGON
            else -> when (sectionRenderingType) {
                ProgramStageSectionRenderingType.SEQUENTIAL -> UiRenderType.SEQUENCIAL
                ProgramStageSectionRenderingType.MATRIX -> UiRenderType.MATRIX
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
                    else -> UiRenderType.DEFAULT
                }
            }
        }
    }
}
