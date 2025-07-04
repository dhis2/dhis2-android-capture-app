package org.dhis2.commons.prefs

import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import java.util.Date

interface PreferenceProvider {

    fun sharedPreferences(): SharedPreferences

    fun saveUserCredentials(serverUrl: String, userName: String, pass: String?)

    fun areCredentialsSet(): Boolean
    fun areSameCredentials(serverUrl: String?, userName: String?): Boolean
    fun clear()
    fun setValue(key: String, value: Any? = null)
    fun secureValue(key: String, value: Any? = null)
    fun getSecureValue(key: String, default: String? = null): String?
    fun removeValue(key: String)
    fun contains(vararg keys: String): Boolean
    fun getString(key: String, default: String? = null): String?
    fun getInt(key: String, default: Int): Int
    fun getLong(key: String, default: Long): Long?
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getFloat(key: String, default: Float): Float?
    fun getSet(key: String, default: Set<String>): Set<String>?
    fun <T> getObjectFromJson(key: String, typeToken: TypeToken<T>, default: T): T
    fun <T> saveAsJson(key: String, objectToSave: T)
    fun lastMetadataSync(): Date?
    fun lastDataSync(): Date?
    fun lastSync(): Date?
}
