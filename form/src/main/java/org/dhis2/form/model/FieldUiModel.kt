package org.dhis2.form.model

import java.util.Date
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
        fun showDialog(title: String, message: String?)
        fun showCustomCalendar(uid: String, label: String?, date: Date)
        fun showYearMonthDayPicker(uid: String, year: Int, month: Int, day: Int)
        fun onItemAction(action: RowAction)
    }
}
