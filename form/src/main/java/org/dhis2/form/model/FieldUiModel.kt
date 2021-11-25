package org.dhis2.form.model

import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.event.UiEventFactory
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.style.FormUiModelStyle
import org.hisp.dhis.android.core.common.ValueType

interface FieldUiModel {

    val uid: String

    val layoutId: Int

    val value: String?

    val focused: Boolean

    val error: String?

    val editable: Boolean

    val warning: String?

    val mandatory: Boolean

    val label: String

    val formattedLabel: String

    val programStageSection: String?

    val style: FormUiModelStyle?

    val hint: String?

    val description: String?

    val valueType: ValueType?

    val legend: LegendValue?

    val optionSet: String?

    val allowFutureDates: Boolean?

    val uiEventFactory: UiEventFactory?

    val displayName: String?

    fun setCallback(callback: Callback)

    fun equals(item: FieldUiModel): Boolean

    fun onItemClick()

    fun onNext()

    fun onTextChange(value: String?)

    fun onDescriptionClick()

    fun onClear()

    fun invokeUiEvent()

    fun setValue(value: String?): FieldUiModel

    fun setFocus(): FieldUiModel

    fun setError(error: String?): FieldUiModel

    fun setEditable(editable: Boolean): FieldUiModel

    fun setLegend(legendValue: LegendValue?): FieldUiModel

    fun setWarning(warning: String): FieldUiModel

    fun setFieldMandatory(): FieldUiModel

    fun setDisplayName(displayName: String?): FieldUiModel

    interface Callback {
        fun intent(intent: FormIntent)
        fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents)
    }
}
