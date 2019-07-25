package org.dhis2.data.sharedPreferences

import android.content.Context

class SharePreferencesProviderImpl(val context: Context): SharePreferencesProvider {
    override fun sharedPreferences(): PreferencesRepository {
        return PreferencesRepository(context)
    }
}