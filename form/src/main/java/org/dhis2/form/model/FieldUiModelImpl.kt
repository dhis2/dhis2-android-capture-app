package org.dhis2.form.model

import org.dhis2.form.ui.style.FormUiModelStyle

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
    override val style: FormUiModelStyle? = null
) : FieldUiModel {

    override fun setCallback(callback: FieldUiModel.Callback) {
        TODO("Not yet implemented")
    }

    override fun equals(item: FieldUiModel): Boolean {
        TODO("Not yet implemented")
    }

    override fun onItemClick() {
        TODO("Not yet implemented")
    }

    override fun onNext() {
        TODO("Not yet implemented")
    }

    override fun onTextChange(value: String?) {
        TODO("Not yet implemented")
    }

    override fun setValue(value: String?) = this.copy(value = value)

    override fun setFocus() = this.copy(focused = true)

    override fun setError(error: String?) = this.copy(error = error)

    override fun setEditable(editable: Boolean) = this.copy(editable = editable)

    override fun hasLegend(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setLegend(legendValue: LegendValue?): FieldUiModel {
        TODO("Not yet implemented")
    }

    override fun getOptionSet(): String? {
        TODO("Not yet implemented")
    }

    override fun setWarning(warning: String) = this.copy(warning = warning)

    override fun setFieldMandatory() = this.copy(mandatory = true)
}
