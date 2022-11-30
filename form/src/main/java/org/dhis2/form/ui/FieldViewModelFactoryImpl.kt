package org.dhis2.form.ui

import androidx.databinding.ObservableField
import org.dhis2.commons.extensions.Preconditions.Companion.isNull
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.event.UiEventFactoryImpl
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.HintProvider
import org.dhis2.form.ui.provider.KeyboardActionProvider
import org.dhis2.form.ui.provider.LayoutProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.form.ui.provider.UiEventTypesProvider
import org.dhis2.form.ui.provider.UiStyleProvider
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute

class FieldViewModelFactoryImpl(
    private val noMandatoryFields: Boolean,
    private val uiStyleProvider: UiStyleProvider,
    private val layoutProvider: LayoutProvider,
    private val hintProvider: HintProvider,
    private val displayNameProvider: DisplayNameProvider,
    private val uiEventTypesProvider: UiEventTypesProvider,
    private val keyboardActionProvider: KeyboardActionProvider,
    private val legendValueProvider: LegendValueProvider
) : FieldViewModelFactory {
    private val currentSection = ObservableField("")

    override fun create(
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
        options: List<Option>?,
        featureType: FeatureType?,
        url: String?,
    ): FieldUiModel {
        var isMandatory = mandatory
        isNull(valueType, "type must be supplied")
        if (noMandatoryFields) isMandatory = false
        return FieldUiModelImpl(
            id,
            layoutProvider.getLayoutByType(
                valueType,
                fieldRendering?.type(),
                optionSet,
                renderingType
            ),
            value,
            false,
            null,
            editable,
            null,
            isMandatory,
            label,
            programStageSection,
            uiStyleProvider.provideStyle(valueType),
            hintProvider.provideDateHint(valueType),
            description,
            valueType,
            legendValueProvider.provideLegendValue(id, value),
            optionSet,
            allowFutureDates,
            UiEventFactoryImpl(
                id,
                label,
                description,
                valueType,
                allowFutureDates,
                optionSet
            ),
            displayNameProvider.provideDisplayName(valueType, value, optionSet),
            uiEventTypesProvider.provideUiRenderType(
                featureType,
                fieldRendering?.type(),
                renderingType
            ),
            options,
            keyboardActionProvider.provideKeyboardAction(valueType),
            fieldMask,
            url,
        )
    }

    override fun createForAttribute(
        trackedEntityAttribute: TrackedEntityAttribute,
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute?,
        value: String?,
        editable: Boolean,
        options: List<Option>?
    ): FieldUiModel {
        isNull(trackedEntityAttribute.valueType(), "type must be supplied")
        return create(
            id = trackedEntityAttribute.uid(),
            label = trackedEntityAttribute.displayFormName() ?: "",
            valueType = trackedEntityAttribute.valueType()!!,
            mandatory = programTrackedEntityAttribute?.mandatory() == true,
            optionSet = trackedEntityAttribute.optionSet()?.uid(),
            value = value,
            programStageSection = null,
            allowFutureDates = programTrackedEntityAttribute?.allowFutureDate() ?: true,
            editable = editable,
            renderingType = SectionRenderingType.LISTING,
            description = programTrackedEntityAttribute?.displayDescription()
                ?: trackedEntityAttribute.displayDescription(),
            fieldRendering = programTrackedEntityAttribute?.renderType()?.mobile(),
            optionCount = null,
            objectStyle = trackedEntityAttribute.style() ?: ObjectStyle.builder().build(),
            fieldMask = trackedEntityAttribute.fieldMask(),
            options = options!!,
            featureType = if (trackedEntityAttribute.valueType() === ValueType.COORDINATE) {
                FeatureType.POINT
            } else null,
            url = null
        )
    }

    override fun createSingleSection(singleSectionName: String): FieldUiModel {
        return SectionUiModelImpl(
            SectionUiModelImpl.SINGLE_SECTION_UID,
            layoutProvider.getLayoutForSection(),
            null,
            false,
            null,
            false,
            null,
            false,
            singleSectionName,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true,
            0,
            0,
            0,
            0,
            SectionRenderingType.LISTING.name,
            currentSection,
        )
    }

    override fun createSection(
        sectionUid: String,
        sectionName: String?,
        description: String?,
        isOpen: Boolean,
        totalFields: Int,
        completedFields: Int,
        rendering: String?
    ): FieldUiModel {
        return SectionUiModelImpl(
            sectionUid,
            layoutProvider.getLayoutForSection(),
            null,
            false,
            null,
            false,
            null,
            false,
            sectionName ?: "",
            sectionUid,
            null,
            null,
            description,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            isOpen,
            totalFields,
            completedFields,
            0,
            0,
            rendering,
            currentSection
        )
    }

    override fun createClosingSection(): FieldUiModel {
        return SectionUiModelImpl(
            SectionUiModelImpl.CLOSING_SECTION_UID,
            layoutProvider.getLayoutForSection(),
            null,
            false,
            null,
            false,
            null,
            false,
            SectionUiModelImpl.CLOSING_SECTION_UID,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            0,
            0,
            0,
            0,
            SectionRenderingType.LISTING.name,
            currentSection
        )
    }
}
