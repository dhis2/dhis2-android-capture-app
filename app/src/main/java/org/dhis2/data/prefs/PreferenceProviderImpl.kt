package org.dhis2.data.prefs

import android.content.Context
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.dhis2.utils.Constants
import org.dhis2.utils.Constants.SECURE_CREDENTIALS
import org.dhis2.utils.Constants.SECURE_PASS
import org.dhis2.utils.Constants.SECURE_SERVER_URL
import org.dhis2.utils.Constants.SECURE_USER_NAME

class PreferenceProviderImpl(val context: Context) : PreferenceProvider {

    override fun setValue(key: String, value: Any?) {
        value?.let {
            when (it) {
                is String -> SecurePreferences.setValue(context, key, it)
                is Boolean -> SecurePreferences.setValue(context, key, it)
                is Int -> SecurePreferences.setValue(context, key, it)
                is Long -> SecurePreferences.setValue(context, key, it)
                is Float -> SecurePreferences.setValue(context, key, it)
                is Set<*> -> SecurePreferences.setValue(context, key, it as Set<String>)
            }
        } ?: SecurePreferences.removeValue(context, key)
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
        SecurePreferences.setValue(context, SECURE_CREDENTIALS, true)
        SecurePreferences.setValue(context, SECURE_SERVER_URL, serverUrl)
        SecurePreferences.setValue(context, SECURE_USER_NAME, userName)
        SecurePreferences.setValue(context, SECURE_PASS, pass)
    }

    override fun areCredentialsSet(): Boolean {
        return SecurePreferences.getBooleanValue(context, SECURE_CREDENTIALS, false)
    }

    override fun areSameCredentials(serverUrl: String, userName: String, pass: String): Boolean {
        return SecurePreferences.getStringValue(context, SECURE_SERVER_URL, "") == serverUrl &&
            SecurePreferences.getStringValue(context, SECURE_USER_NAME, "") == userName &&
            SecurePreferences.getStringValue(context, SECURE_PASS, "") == pass
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
