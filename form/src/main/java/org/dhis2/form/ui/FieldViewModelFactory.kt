package org.dhis2.form.ui

import org.dhis2.commons.intents.CustomIntentAction
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.PeriodSelector
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering
import org.hisp.dhis.android.core.program.SectionRenderingType
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates

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
        featureType: FeatureType? = null,
        autoCompleteList: List<String>? = null,
        orgUnitSelectorScope: OrgUnitSelectorScope? = null,
        selectableDates: SelectableDates? = null,
        eventCategories: List<EventCategory>? = null,
        periodSelector: PeriodSelector? = null,
        customIntentAction: CustomIntentAction? = null
    ): FieldUiModel

    fun createSingleSection(singleSectionName: String): FieldUiModel

    fun createSection(
        sectionUid: String,
        sectionName: String?,
        description: String?,
        isOpen: Boolean,
        totalFields: Int,
        completedFields: Int,
        rendering: String?,
    ): FieldUiModel

    fun createClosingSection(): FieldUiModel
}
