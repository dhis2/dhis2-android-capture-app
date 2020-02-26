package org.dhis2.data.prefs

import android.content.SharedPreferences

interface PreferenceProvider {

    fun sharedPreferences(): SharedPreferences

    fun saveUserCredentials(serverUrl: String, userName: String, pass: String)

    fun areCredentialsSet(): Boolean
    fun areSameCredentials(serverUrl: String, userName: String, pass: String): Boolean
    fun saveJiraCredentials(jiraAuth: String): String
    fun saveJiraUser(jiraUser: String)
    fun closeJiraSession()
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
    fun saveGroupingForProgram(programUid: String, shouldGroup: Boolean)
    fun programHasGrouping(programUid: String): Boolean
}
