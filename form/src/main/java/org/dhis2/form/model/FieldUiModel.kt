package org.dhis2.form.model

interface FieldUiModel {

    fun setCallback(callback: Callback)

    fun getLayoutId(): Int

    fun getUid(): String

    fun equals(item: FieldUiModel): Boolean

    fun onItemClick()

    fun onNext()

    fun onTextChange(value: String?)

    fun setValue(value: String?): FieldUiModel

    fun setFocus(): FieldUiModel

    fun getError(): String?

    fun setError(error: String?): FieldUiModel

    fun isFocused(): Boolean

    fun isEditable(): Boolean

    fun setEditMode(value: Boolean): FieldUiModel

    fun setEditable(editable: Boolean): FieldUiModel

    fun hasLegend(): Boolean

    fun setLegend(legendValue: LegendValue?): FieldUiModel

    fun getProgramStageSection(): String?

    fun getValue(): String?

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
