package org.dhis2.common.preferences

import org.dhis2.data.prefs.PreferenceProvider

class PreferencesRobot(val preferences: PreferenceProvider) {

    fun cleanPreferences(){
        preferences.clear()
    }
}