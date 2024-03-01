package org.dhis2.form.ui

import androidx.databinding.ObservableField
import org.dhis2.commons.extensions.Preconditions.Companion.isNull
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.event.UiEventFactoryImpl
import org.dhis2.form.ui.provider.AutoCompleteProvider
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
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates

class FieldViewModelFactoryImpl(
    private val uiStyleProvider: UiStyleProvider,
    private val layoutProvider: LayoutProvider,
    private val hintProvider: HintProvider,
    private val displayNameProvider: DisplayNameProvider,
    private val uiEventTypesProvider: UiEventTypesProvider,
    private val keyboardActionProvider: KeyboardActionProvider,
    private val legendValueProvider: LegendValueProvider,
    private val autoCompleteProvider: AutoCompleteProvider,
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
        objectStyle: ObjectStyle,
        fieldMask: String?,
        optionSetConfiguration: OptionSetConfiguration?,
        featureType: FeatureType?,
        autoCompleteList: List<String>?,
        orgUnitSelectorScope: OrgUnitSelectorScope?,
        selectableDates: SelectableDates?,
    ): FieldUiModel {
        isNull(valueType, "type must be supplied")
        return FieldUiModelImpl(
            uid = id,
            layoutId = layoutProvider.getLayoutByType(
                valueType,
                fieldRendering?.type(),
                optionSet,
                renderingType,
            ),
            value = value,
            focused = false,
            error = null,
            editable = editable,
            warning = null,
            mandatory = mandatory,
            label = label,
            programStageSection = programStageSection,
            style = uiStyleProvider.provideStyle(valueType),
            hint = hintProvider.provideDateHint(valueType),
            description = description,
            valueType = if (optionSet != null && valueType == ValueType.TEXT) ValueType.MULTI_TEXT else valueType,
            legend = legendValueProvider.provideLegendValue(id, value),
            optionSet = if (valueType == ValueType.MULTI_TEXT) null else optionSet,
            allowFutureDates = allowFutureDates,
            uiEventFactory = UiEventFactoryImpl(
                id,
                label,
                description,
                valueType,
                allowFutureDates,
                optionSet,
            ),
            displayName = displayNameProvider.provideDisplayName(valueType, value, optionSet),
            renderingType = uiEventTypesProvider.provideUiRenderType(
                featureType,
                fieldRendering?.type(),
                renderingType,
            ),
            optionSetConfiguration = optionSetConfiguration,
            keyboardActionType = keyboardActionProvider.provideKeyboardAction(valueType),
            fieldMask = fieldMask,
            autocompleteList = autoCompleteProvider.provideAutoCompleteValues(id),
            orgUnitSelectorScope = orgUnitSelectorScope,
            selectableDates = selectableDates,
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
        rendering: String?,
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
            isOpen,
            totalFields,
            completedFields,
            0,
            0,
            rendering,
            currentSection,
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
            false,
            0,
            0,
            0,
            0,
            SectionRenderingType.LISTING.name,
            currentSection,
        )
    }
}
