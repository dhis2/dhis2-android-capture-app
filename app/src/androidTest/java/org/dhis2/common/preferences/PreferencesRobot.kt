package org.dhis2.common.preferences

import android.content.SharedPreferences
import org.dhis2.commons.prefs.PreferenceProvider

class PreferencesRobot(private val preferences: PreferenceProvider,
                       private val sdkLocalDBPreferences: SharedPreferences) {

    fun saveValue(key: String, value: Any? = null) {
        preferences.setValue(key, value)
    }

    fun saveValueToSDKPreferences(key: String, value: String) {
        sdkLocalDBPreferences.edit().putString(key, value).apply()
    }

    fun cleanPreferences() {
        preferences.clear()
        sdkLocalDBPreferences.edit().clear()
    }
}
