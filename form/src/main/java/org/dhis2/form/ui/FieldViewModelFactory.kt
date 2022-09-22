package org.dhis2.form.ui

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute

interface FieldViewModelFactory {
    fun create(
        id: String,
        label: String,
        valueType: ValueType,
        mandatory: Boolean,
        optionSet: String? = null,
        value: String? = null,
        programStageSection: String? = null,
        allowFutureDates: Boolean? = null,
        editable: Boolean,
        renderingType: SectionRenderingType? = null,
        description: String?,
        fieldRendering: ValueTypeDeviceRendering? = null,
        objectStyle: ObjectStyle = ObjectStyle.builder().build(),
        fieldMask: String? = null,
        optionSetConfiguration: OptionSetConfiguration? = null,
        featureType: FeatureType? = null
    ): FieldUiModel

    fun createForAttribute(
        trackedEntityAttribute: TrackedEntityAttribute,
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute?,
        value: String?,
        editable: Boolean,
        optionSetConfiguration: OptionSetConfiguration?
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
