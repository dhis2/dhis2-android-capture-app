package org.dhis2.form.ui.provider

import org.dhis2.commons.prefs.PreferenceProvider

class AutoCompleteProviderImpl(
    val preferenceProvider: PreferenceProvider,
) : AutoCompleteProvider {
    override fun provideAutoCompleteValues(elementUid: String): List<String>? = preferenceProvider.getList(elementUid, emptyList())
}
