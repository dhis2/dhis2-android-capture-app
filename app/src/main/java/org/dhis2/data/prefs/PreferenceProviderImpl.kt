package org.dhis2.data.prefs

import android.content.Context
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.dhis2.utils.Constants

class PreferenceProviderImpl(val context: Context) : PreferenceProvider {

    override fun setValue(key: String, value: Any?) {
        if (value == null)
            SecurePreferences.removeValue(context, key)
        else
            when (value) {
                is String -> SecurePreferences.setValue(context, key, value)
                is Boolean -> SecurePreferences.setValue(context, key, value)
                is Int -> SecurePreferences.setValue(context, key, value)
                is Long -> SecurePreferences.setValue(context, key, value)
                is Float -> SecurePreferences.setValue(context, key, value)
                is Set<*> -> SecurePreferences.setValue(context, key, value as Set<String>)
            }
    }

    override fun getString(key: String, default: String?): String? {
        return SecurePreferences.getStringValue(context, key, default)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return SecurePreferences.getBooleanValue(context, key, default)
    }

    override fun getInt(key: String, default: Int): Int {
        return SecurePreferences.getIntValue(context, key, default)
    }

    override fun getLong(key: String, default: Long): Long? {
        return SecurePreferences.getLongValue(context, key, default)
    }

    override fun getFloat(key: String, default: Float): Float? {
        return SecurePreferences.getFloatValue(context, key, default)
    }

    override fun getSet(key: String, default: Set<String>): Set<String>? {
        return SecurePreferences.getStringSetValue(context, key, default)
    }

    override fun contains(vararg keys: String): Boolean {
        return keys.all { SecurePreferences.contains(context, it) }
    }

    override fun saveUserCredentials(serverUrl: String, userName: String, pass: String) {
        SecurePreferences.setValue(context, Constants.SECURE_CREDENTIALS, true)
        SecurePreferences.setValue(context, Constants.SECURE_SERVER_URL, serverUrl)
        SecurePreferences.setValue(context, Constants.SECURE_USER_NAME, userName)
        SecurePreferences.setValue(context, Constants.SECURE_PASS, pass)
    }

    override fun areCredentialsSet(): Boolean {
        return SecurePreferences.getBooleanValue(context, Constants.SECURE_CREDENTIALS, false)
    }

    override fun areSameCredentials(serverUrl: String, userName: String, pass: String): Boolean {
        return SecurePreferences.getStringValue(context, Constants.SECURE_SERVER_URL, "") == serverUrl &&
                SecurePreferences.getStringValue(context, Constants.SECURE_USER_NAME, "") == userName &&
                SecurePreferences.getStringValue(context, Constants.SECURE_PASS, "") == pass
    }

    override fun saveJiraCredentials(jiraAuth: String): String {
        SecurePreferences.setValue(context, Constants.JIRA_AUTH, jiraAuth)
        return String.format("Basic %s", jiraAuth)
    }

    override fun saveJiraUser(jiraUser: String) {
        SecurePreferences.setValue(context, Constants.JIRA_USER, jiraUser)
    }

    override fun closeJiraSession() {
        SecurePreferences.removeValue(context, Constants.JIRA_AUTH)
    }

    override fun clear() {
        SecurePreferences.clearAllValues(context)
    }
}