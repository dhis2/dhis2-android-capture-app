package org.dhis2.data.forms.dataentry.fields

interface FieldUiModel {

    fun setCallback(callback: Callback)

    fun getLayoutId(): Int

    fun getUid(): String

    fun equals(item: FieldUiModel): Boolean

    fun onActivate()

    fun onDeactivate()

    interface Callback {

        fun onNext()

        fun showDialog(title: String, message: String?)

        fun onClick()
    }
}
