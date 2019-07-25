package org.dhis2.data.sharedPreferences

import androidx.annotation.NonNull

interface SharePreferencesProvider{
    @NonNull
    fun sharedPreferences(): PreferencesRepository
}