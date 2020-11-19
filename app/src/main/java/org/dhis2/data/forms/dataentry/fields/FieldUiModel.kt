package org.dhis2.data.forms.dataentry.fields

interface FieldUiModel {

    fun getLayoutId(): Int

    fun getUid(): String

    fun equals(item: FieldUiModel): Boolean
}
