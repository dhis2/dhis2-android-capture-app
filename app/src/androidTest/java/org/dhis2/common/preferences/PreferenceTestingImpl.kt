package org.dhis2.common.preferences

import android.content.Context
import android.content.SharedPreferences
import org.dhis2.commons.prefs.PreferenceProviderImpl
import org.dhis2.commons.Constants
import org.dhis2.mobile.commons.providers.createSecureSharedPreferences

class PreferenceTestingImpl(context: Context) : PreferenceProviderImpl(context) {

    private val sharedPreferences: SharedPreferences =
        createSecureSharedPreferences(context, Constants.SHARE_PREFS)

    override fun setValue(key: String, value: Any?) {
        value?.let {
            when (it) {
                is String -> {
                    sharedPreferences.edit().putString(key, it).apply()
                }
                is Boolean -> {
                    sharedPreferences.edit().putBoolean(key, it).apply()
                }
                is Int -> {
                    sharedPreferences.edit().putInt(key, it).apply()
                }
                is Long -> {
                    sharedPreferences.edit().putLong(key, it).apply()
                }
                is Float -> {
                    sharedPreferences.edit().putFloat(key, it).apply()
                }
                is Set<*> -> {
                    sharedPreferences.edit().putStringSet(key, it as Set<String>).apply()
                }
            }
        } ?: run {
            sharedPreferences.edit().clear().apply()
        }
    }
}
