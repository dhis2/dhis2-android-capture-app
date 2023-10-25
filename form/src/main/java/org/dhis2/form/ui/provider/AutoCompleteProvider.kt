package org.dhis2.form.ui.provider

fun interface AutoCompleteProvider {

    fun provideAutoCompleteValues(elementUid: String): List<String>?
}
