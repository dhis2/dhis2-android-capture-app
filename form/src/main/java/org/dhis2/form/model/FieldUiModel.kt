package org.dhis2.form.model

import org.dhis2.form.ui.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.style.FormUiModelStyle

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

    val programStageSection: String?

    val style: FormUiModelStyle?

    fun setCallback(callback: Callback)

    fun equals(item: FieldUiModel): Boolean

    fun onItemClick()

    fun onNext()

    fun onTextChange(value: String?)

    fun setValue(value: String?): FieldUiModel

    fun setFocus(): FieldUiModel

    fun setError(error: String?): FieldUiModel

    fun setEditable(editable: Boolean): FieldUiModel

    fun hasLegend(): Boolean

    fun setLegend(legendValue: LegendValue?): FieldUiModel

    fun getOptionSet(): String?

    fun setWarning(warning: String): FieldUiModel

    fun setFieldMandatory(): FieldUiModel

    interface Callback {
        fun onNext()
        fun intent(intent: FormIntent)
        fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents)
        fun onItemAction(action: RowAction)
        fun currentLocation(coordinateFieldUid: String)
        fun mapRequest(
            coordinateFieldUid: String,
            featureType: String,
            initialCoordinates: String?
        )
    }
}
