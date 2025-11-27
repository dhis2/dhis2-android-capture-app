package org.dhis2.form.model

import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.event.UiEventFactory
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates

interface FieldUiModel {
    val uid: String

    val value: String?

    val focused: Boolean

    val error: String?

    val editable: Boolean

    val warning: String?

    val mandatory: Boolean

    val label: String

    val formattedLabel: String

    val programStageSection: String?

    val hint: String?

    val description: String?

    val valueType: ValueType?

    val legend: LegendValue?

    val optionSet: String?

    val allowFutureDates: Boolean?

    val uiEventFactory: UiEventFactory?

    val displayName: String?

    val renderingType: UiRenderType?

    val optionSetConfiguration: OptionSetConfiguration?

    var customIntent: CustomIntentModel?

    val keyboardActionType: KeyboardActionType?

    val fieldMask: String?

    val isAffirmativeChecked: Boolean

    val isNegativeChecked: Boolean

    val isLoadingData: Boolean

    val autocompleteList: List<String>?

    val orgUnitSelectorScope: OrgUnitSelectorScope?

    val selectableDates: SelectableDates?

    val eventCategories: List<EventCategory>?

    val periodSelector: PeriodSelector?

    fun setCallback(callback: Callback)

    fun equals(item: FieldUiModel): Boolean

    fun onItemClick()

    fun onClear()

    fun onSave(value: String?)

    fun invokeUiEvent(uiEventType: UiEventType)

    fun invokeIntent(intent: FormIntent)

    fun setValue(value: String?): FieldUiModel

    fun setSelectableDates(selectableDates: SelectableDates?): FieldUiModel

    fun setIsLoadingData(isLoadingData: Boolean): FieldUiModel

    fun setFocus(): FieldUiModel

    fun setError(error: String?): FieldUiModel

    fun setEditable(editable: Boolean): FieldUiModel

    fun setLegend(legendValue: LegendValue?): FieldUiModel

    fun setWarning(warning: String): FieldUiModel

    fun setFieldMandatory(): FieldUiModel

    fun setDisplayName(displayName: String?): FieldUiModel

    fun setKeyBoardActionDone(): FieldUiModel

    fun setOptionSetConfiguration(optionSetConfiguration: OptionSetConfiguration): FieldUiModel

    interface Callback {
        fun intent(intent: FormIntent)

        fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents)
    }

    fun isSection() = valueType == null

    fun isSectionWithFields(): Boolean
}
