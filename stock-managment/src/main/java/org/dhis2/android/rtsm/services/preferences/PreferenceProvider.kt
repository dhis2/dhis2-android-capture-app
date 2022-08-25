package org.dhis2.android.rtsm.services.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore

interface PreferenceProvider {
    fun sharedPreferences(): SharedPreferences
    fun preferenceDataStore(context: Context): PreferenceDataStore

    fun saveUserCredentials(serverUrl: String, userName: String, pass: String)
    fun clear()

    fun setValue(key: String, value: Any? = null)
    fun removeValue(key: String)

    fun contains(vararg keys: String): Boolean
    fun getString(key: String, default: String? = null): String?
    fun getStringSet(key: String, defValues: Set<String>? = null): Set<String>?
    fun getInt(key: String, default: Int): Int
    fun getLong(key: String, default: Long): Long
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getFloat(key: String, default: Float): Float
}