package org.dhis2.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.dhis2.utils.Constants
import org.dhis2.utils.Constants.SECURE_CREDENTIALS
import org.dhis2.utils.Constants.SECURE_PASS
import org.dhis2.utils.Constants.SECURE_SERVER_URL
import org.dhis2.utils.Constants.SECURE_USER_NAME

class PreferenceProviderImpl(val context: Context) : PreferenceProvider {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)

    override fun sharedPreferences(): SharedPreferences {
        return sharedPreferences
    }

    override fun setValue(key: String, value: Any?) {
        value?.let {
            when (it) {
                is String -> {
                    SecurePreferences.setValue(context, key, it)
                    sharedPreferences.edit().putString(key, it).apply()
                }
                is Boolean -> {
                    SecurePreferences.setValue(context, key, it)
                    sharedPreferences.edit().putBoolean(key, it).apply()
                }
                is Int -> {
                    SecurePreferences.setValue(context, key, it)
                    sharedPreferences.edit().putInt(key, it).apply()
                }
                is Long -> {
                    SecurePreferences.setValue(context, key, it)
                    sharedPreferences.edit().putLong(key, it).apply()
                }
                is Float -> {
                    SecurePreferences.setValue(context, key, it)
                    sharedPreferences.edit().putFloat(key, it).apply()
                }
                is Set<*> -> {
                    SecurePreferences.setValue(context, key, it as MutableSet<String>)
                    sharedPreferences.edit().putStringSet(key, it as Set<String>).apply()
                }
            }
        } ?: run {
            SecurePreferences.removeValue(context, key)
            sharedPreferences.edit().clear().apply()
        }
    }

    override fun removeValue(key: String) {
        if (contains(key)) {
            SecurePreferences.removeValue(context, key)
            sharedPreferences.edit().remove(key).apply()
        }
    }

    override fun getString(key: String, default: String?): String? {
        return SecurePreferences.getStringValue(
            context, key,
            sharedPreferences.getString(key, default)
        )
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return SecurePreferences.getBooleanValue(
            context, key,
            sharedPreferences.getBoolean(key, default)
        )
    }

    override fun getInt(key: String, default: Int): Int {
        return SecurePreferences.getIntValue(
            context, key,
            sharedPreferences.getInt(key, default)
        )
    }

    override fun getLong(key: String, default: Long): Long? {
        return SecurePreferences.getLongValue(
            context, key,
            sharedPreferences.getLong(key, default)
        )
    }

    override fun getFloat(key: String, default: Float): Float? {
        return SecurePreferences.getFloatValue(
            context, key,
            sharedPreferences.getFloat(key, default)
        )
    }

    override fun getSet(key: String, default: Set<String>): Set<String>? {
        return SecurePreferences.getStringSetValue(
            context, key,
            default
        )
    }

    override fun contains(vararg keys: String): Boolean {
        return keys.all {
            SecurePreferences.contains(context, it) || sharedPreferences.contains(it)
        }
    }

    override fun saveUserCredentials(serverUrl: String, userName: String, pass: String) {
        SecurePreferences.setValue(context, SECURE_CREDENTIALS, true)
        SecurePreferences.setValue(context, SECURE_SERVER_URL, serverUrl)
        SecurePreferences.setValue(context, SECURE_USER_NAME, userName)
        SecurePreferences.setValue(context, SECURE_PASS, pass)
    }

    override fun areCredentialsSet(): Boolean {
        return SecurePreferences.getBooleanValue(
            context, SECURE_CREDENTIALS,
            sharedPreferences.getBoolean(SECURE_CREDENTIALS, false)
        )
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
        sharedPreferences.edit().clear().apply()
    }

    override fun <T> saveAsJson(key: String, objectToSave: T) {
        setValue(key, Gson().toJson(objectToSave))
    }

    override fun <T> getObjectFromJson(key: String, typeToken: TypeToken<T>, default: T): T {
        val mapTypeToken = typeToken.type
        return if (getString(key, null) == null) {
            default
        } else {
            Gson().fromJson<T>(getString(key), mapTypeToken)
        }
    }
    /*endregion*/
}
