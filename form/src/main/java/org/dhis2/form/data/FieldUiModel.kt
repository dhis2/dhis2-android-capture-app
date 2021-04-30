package org.dhis2.form.data

interface FieldUiModel {

    fun setCallback(callback: Callback)

    fun getLayoutId(): Int

    fun getUid(): String

    fun equals(item: FieldUiModel): Boolean

    fun onItemClick()

    fun onNext()

    fun onTextChange(value: String?)

    interface Callback {

        fun onNext()
        fun showDialog(title: String, message: String?)
    }
}
