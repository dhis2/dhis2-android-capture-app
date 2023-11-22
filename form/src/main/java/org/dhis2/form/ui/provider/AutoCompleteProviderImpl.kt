package org.dhis2.form.ui.provider

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.dhis2.commons.prefs.PreferenceProvider

class AutoCompleteProviderImpl(
    val preferenceProvider: PreferenceProvider,
) : AutoCompleteProvider {

    override fun provideAutoCompleteValues(elementUid: String): List<String>? {
        return getListFromPreference(elementUid)
    }

    private fun getListFromPreference(uid: String): MutableList<String> {
        val gson = Gson()
        val json = preferenceProvider.sharedPreferences().getString(uid, "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
}
