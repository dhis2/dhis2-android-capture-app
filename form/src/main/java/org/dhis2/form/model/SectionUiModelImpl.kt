package org.dhis2.form.model

import androidx.databinding.ObservableField
import org.dhis2.commons.intents.CustomIntentAction
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.form.ui.event.UiEventFactory
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.intent.FormIntent.OnFocus
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates

data class SectionUiModelImpl(
    override val uid: String,
    override val value: String? = null,
    override val focused: Boolean = false,
    override val error: String? = null,
    override val editable: Boolean = true,
    override val warning: String? = null,
    override val mandatory: Boolean = false,
    override val label: String,
    override val programStageSection: String? = null,
    override val hint: String? = null,
    override val description: String? = null,
    override val valueType: ValueType? = null,
    override val legend: LegendValue? = null,
    override val optionSet: String? = null,
    override val allowFutureDates: Boolean? = null,
    override val uiEventFactory: UiEventFactory? = null,
    override val displayName: String? = null,
    override val renderingType: UiRenderType? = null,
    override val keyboardActionType: KeyboardActionType? = null,
    override val fieldMask: String? = null,
    var isOpen: Boolean? = false,
    var totalFields: Int = 0,
    var completedFields: Int = 0,
    var errors: Int = 0,
    var warnings: Int = 0,
    var rendering: String? = null,
    var selectedField: ObservableField<String?> = ObservableField(null),
    override val isLoadingData: Boolean = false,
    override var optionSetConfiguration: OptionSetConfiguration? = null,
    override val autocompleteList: List<String>? = null,
    override val orgUnitSelectorScope: OrgUnitSelectorScope? = null,
    override val selectableDates: SelectableDates? = null,
    override val eventCategories: List<EventCategory>? = null,
    override val periodSelector: PeriodSelector? = null,
    override val customIntentAction: CustomIntentAction? = null,
) : FieldUiModel {

    private var sectionNumber: Int = 0
    private var showBottomShadow: Boolean = false
    private var lastPositionShouldChangeHeight: Boolean = false

    private var callback: FieldUiModel.Callback? = null

    fun hasToShowDescriptionIcon(isTitleEllipsed: Boolean): Boolean {
        return !description.isNullOrEmpty() || isTitleEllipsed
    }

    private fun isClosingSection(): Boolean = uid == CLOSING_SECTION_UID

    fun isSelected(): Boolean = selectedField.get() == uid

    fun setShowBottomShadow(showBottomShadow: Boolean) {
        this.showBottomShadow = showBottomShadow
    }

    fun showNextButton(): Boolean {
        return showBottomShadow && !isClosingSection()
    }

    override val formattedLabel: String
        get() = label

    override fun setCallback(callback: FieldUiModel.Callback) {
        this.callback = callback
    }

    override fun onItemClick() {
        callback!!.intent(
            OnFocus(
                uid,
                value,
            ),
        )
    }

    override fun invokeUiEvent(uiEventType: UiEventType) {
        onItemClick()
    }

    override fun invokeIntent(intent: FormIntent) {
        callback?.intent(intent)
    }

    override val isAffirmativeChecked: Boolean
        get() = false

    override val isNegativeChecked: Boolean
        get() = false

    override fun onClear() {
        // Not necessary in this implementation
    }

    override fun onSave(value: String?) {
        // Not necessary in this implementation
    }

    override fun setValue(value: String?) = this.copy(value = value)

    override fun setSelectableDates(selectableDates: SelectableDates?): FieldUiModel = this.copy(selectableDates = selectableDates)

    override fun setIsLoadingData(isLoadingData: Boolean) = this.copy(isLoadingData = isLoadingData)

    override fun setDisplayName(displayName: String?) = this.copy(displayName = displayName)

    override fun setKeyBoardActionDone() = this.copy(keyboardActionType = KeyboardActionType.DONE)

    override fun setFocus() = this.copy(focused = true)

    override fun setError(error: String?) = this.copy(error = error)

    override fun setEditable(editable: Boolean) = this.copy(editable = editable)

    override fun setLegend(legendValue: LegendValue?) = this.copy(legend = legend)

    override fun setWarning(warning: String) = this.copy(warning = warning)

    override fun setFieldMandatory() = this.copy(mandatory = true)

    override fun isSectionWithFields() = totalFields > 0

    override fun equals(item: FieldUiModel): Boolean {
        item as SectionUiModelImpl
        return super.equals(item) &&
            this.showBottomShadow == item.showBottomShadow &&
            this.lastPositionShouldChangeHeight == item.lastPositionShouldChangeHeight &&
            this.isOpen == item.isOpen &&
            this.totalFields == item.totalFields &&
            this.completedFields == item.completedFields &&
            this.errors == item.errors &&
            this.warnings == item.warnings &&
            this.sectionNumber == item.sectionNumber
    }

    companion object {
        const val SINGLE_SECTION_UID = "SINGLE_SECTION_UID"
        const val CLOSING_SECTION_UID = "closing_section"
    }
}
