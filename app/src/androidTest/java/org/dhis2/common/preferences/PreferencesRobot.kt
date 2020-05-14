package org.dhis2.common.preferences

import org.dhis2.data.prefs.PreferenceProvider

class PreferencesRobot(private val preferences: PreferenceProvider) {

    fun saveValue(key: String, value: Any? = null){
        preferences.setValue(key, value)
    }

    fun cleanPreferences(){
        preferences.clear()
    }
}