package org.dhis2.form.model

import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.event.UiEventFactory
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.style.FormUiModelStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import java.io.File

data class FieldUiModelImpl(
    override val uid: String,
    override val layoutId: Int,
    override val value: String? = null,
    override val focused: Boolean = false,
    override val error: String? = null,
    override val editable: Boolean = true,
    override val warning: String? = null,
    override val mandatory: Boolean = false,
    override val label: String,
    override val programStageSection: String? = null,
    override val style: FormUiModelStyle? = null,
    override val hint: String? = null,
    override val description: String? = null,
    override val valueType: ValueType,
    override val legend: LegendValue? = null,
    override val optionSet: String? = null,
    override val allowFutureDates: Boolean? = null,
    override val uiEventFactory: UiEventFactory? = null,
    override val displayName: String? = null,
    override val renderingType: UiRenderType? = null,
    override val keyboardActionType: KeyboardActionType? = null,
    override val fieldMask: String? = null,
    override val isLoadingData: Boolean = false,
    override var optionSetConfiguration: OptionSetConfiguration?,
    override var autocompleteList: List<String>?,
    override val orgUnitSelectorScope: OrgUnitSelectorScope? = null,
    override val selectableDates: SelectableDates? = null,
) : FieldUiModel {

    private var callback: FieldUiModel.Callback? = null

    override val formattedLabel: String
        get() = if (mandatory) "$label *" else label

    override fun setCallback(callback: FieldUiModel.Callback) {
        this.callback = callback
    }

    override fun onItemClick() {
        callback?.intent(FormIntent.OnFocus(uid, value))
    }

    override fun onNext() {
        callback?.intent(FormIntent.OnNext(uid, value))
    }

    override fun onTextChange(value: CharSequence?) {
        val text = when {
            value?.isEmpty() == true -> null
            else -> value?.toString()
        }
        callback?.intent(FormIntent.OnTextChange(uid, text, valueType))
    }

    override fun onDescriptionClick() {
        callback?.recyclerViewUiEvents(
            RecyclerViewUiEvents.ShowDescriptionLabelDialog(
                label,
                description,
            ),
        )
    }

    override fun onClear() {
        onItemClick()
        callback?.intent(FormIntent.ClearValue(uid))
    }

    override fun onSave(value: String?) {
        onItemClick()
        callback?.intent(FormIntent.OnSave(uid, value, valueType))
    }

    override fun onSaveBoolean(boolean: Boolean) {
        onItemClick()
        val result = when {
            value == null || value != boolean.toString() -> boolean.toString()
            else -> null
        }
        callback?.intent(FormIntent.OnSave(uid, result, valueType))
    }

    override fun onSaveOption(option: Option) {
        val nextValue = when (displayName) {
            option.displayName() -> null
            else -> option.code()
        }
        callback?.intent(FormIntent.OnSave(uid, nextValue, valueType))
    }

    override fun invokeUiEvent(uiEventType: UiEventType) {
        callback?.intent(FormIntent.OnRequestCoordinates(uid))
        if (uiEventType != UiEventType.QR_CODE &&
            uiEventType != UiEventType.EMAIL &&
            uiEventType != UiEventType.PHONE_NUMBER &&
            !focused
        ) {
            onItemClick()
        }
        uiEventFactory?.generateEvent(value, uiEventType, renderingType, this)?.let {
            callback?.recyclerViewUiEvents(it)
        }
    }

    override fun invokeIntent(intent: FormIntent) {
        callback?.intent(intent)
    }

    override val textColor: Int?
        get() = style?.textColor(error, warning)

    override val backGroundColor: Pair<Array<Int>, Int?>?
        get() = style?.backgroundColor(valueType, error, warning)

    override val hasImage: Boolean
        get() = value?.let { File(it).exists() } ?: false

    override val isAffirmativeChecked: Boolean
        get() = value?.toBoolean() == true

    override val isNegativeChecked: Boolean
        get() = value?.toBoolean() == false

    override fun setValue(value: String?) = this.copy(value = value)

    override fun setSelectableDates(selectableDates: SelectableDates?) = this.copy(selectableDates = selectableDates)

    override fun setIsLoadingData(isLoadingData: Boolean) = this.copy(isLoadingData = isLoadingData)

    override fun setDisplayName(displayName: String?) = this.copy(displayName = displayName)

    override fun setKeyBoardActionDone() = this.copy(keyboardActionType = KeyboardActionType.DONE)
    override fun isSectionWithFields(): Boolean = false

    override fun setFocus() = this.copy(focused = true)

    override fun setError(error: String?) = this.copy(error = error)

    override fun setEditable(editable: Boolean) = this.copy(editable = editable)

    override fun setLegend(legendValue: LegendValue?) = this.copy(legend = legendValue)

    override fun setWarning(warning: String) = this.copy(warning = warning)

    override fun setFieldMandatory() = this.copy(mandatory = true)

    override fun equals(item: FieldUiModel): Boolean {
        if (this === item) return true
        if (javaClass != item.javaClass) return false

        item as FieldUiModelImpl

        if (uid != item.uid) return false
        if (layoutId != item.layoutId) return false
        if (value != item.value) return false
        if (focused != item.focused) return false
        if (error != item.error) return false
        if (editable != item.editable) return false
        if (warning != item.warning) return false
        if (mandatory != item.mandatory) return false
        if (label != item.label) return false
        if (programStageSection != item.programStageSection) return false
        if (style != item.style) return false
        if (hint != item.hint) return false
        if (description != item.description) return false
        if (valueType != item.valueType) return false
        if (legend != item.legend) return false
        if (optionSet != item.optionSet) return false
        if (allowFutureDates != item.allowFutureDates) return false
        if (callback != item.callback) return false
        if (selectableDates != item.selectableDates) return false

        return true
    }
}
