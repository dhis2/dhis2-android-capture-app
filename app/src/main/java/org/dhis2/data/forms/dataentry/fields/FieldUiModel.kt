package org.dhis2.data.forms.dataentry.fields

interface FieldUiModel {

    fun setCallback(callback: Callback)

    fun getLayoutId(): Int

    fun getUid(): String

    fun equals(item: FieldUiModel): Boolean

    fun onItemClick()

    fun onNext()

    interface Callback {

        fun showDialog(title: String, message: String?)
    }

    interface OnNextCallback {
        fun onNext()
    }
}
