package org.dhis2.commons.prefs

import com.google.gson.reflect.TypeToken

interface BasicPreferenceProvider {

    fun clear()
    fun setValue(key: String, value: Any? = null)
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
}