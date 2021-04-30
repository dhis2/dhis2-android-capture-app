package org.dhis2.form.model

interface FieldUiModel {

    val uid: String

    val layoutId: Int

    val value: String?

    val focused: Boolean

    val error: String?

    val editable: Boolean

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

    fun getProgramStageSection(): String?

    fun getOptionSet(): String?

    fun getWarning() : String?

    fun setWarning(warning: String): FieldUiModel

    fun setFieldMandatory(): FieldUiModel

    fun isMandatory(): Boolean

    fun getLabel(): String

    interface Callback {

        fun onNext()
        fun showDialog(title: String, message: String?)
    }
}
