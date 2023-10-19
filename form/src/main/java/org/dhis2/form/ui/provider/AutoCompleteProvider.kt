package org.dhis2.form.ui.provider

interface AutoCompleteProvider {

    fun provideAutoCompleteValues(elementUid: String): List<String>?
}
