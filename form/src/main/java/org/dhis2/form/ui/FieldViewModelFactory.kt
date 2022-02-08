package org.dhis2.form.ui

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.LegendValue
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute

interface FieldViewModelFactory {
    fun create(
        id: String,
        label: String,
        valueType: ValueType,
        mandatory: Boolean,
        optionSet: String?,
        value: String?,
        programStageSection: String?,
        allowFutureDates: Boolean?,
        editable: Boolean,
        renderingType: SectionRenderingType?,
        description: String?,
        fieldRendering: ValueTypeDeviceRendering?,
        optionCount: Int?,
        objectStyle: ObjectStyle,
        fieldMask: String?,
        legendValue: LegendValue?,
        options: List<Option>?,
        featureType: FeatureType?
    ): FieldUiModel

    fun createForAttribute(
        trackedEntityAttribute: TrackedEntityAttribute,
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute?,
        value: String?,
        editable: Boolean,
        options: List<Option>?
    ): FieldUiModel

    fun createSingleSection(singleSectionName: String): FieldUiModel

    fun createSection(
        sectionUid: String,
        sectionName: String?,
        description: String?,
        isOpen: Boolean,
        totalFields: Int,
        completedFields: Int,
        rendering: String?
    ): FieldUiModel

    fun createClosingSection(): FieldUiModel
}
