package org.dhis2.commons.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.dhis2.commons.date.DateUtils
import java.util.Date

const val LAST_META_SYNC = "last_meta_sync"
const val LAST_DATA_SYNC = "last_data_sync"

open class PreferenceProviderImpl(context: Context) : PreferenceProvider {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARE_PREFS, Context.MODE_PRIVATE)

    override fun sharedPreferences(): SharedPreferences {
        return sharedPreferences
    }

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

    override fun removeValue(key: String) {
        if (contains(key)) {
            sharedPreferences.edit().remove(key).apply()
        }
    }

    override fun getString(key: String, default: String?): String? {
        return sharedPreferences.getString(key, default)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    override fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    override fun getLong(key: String, default: Long): Long? {
        return sharedPreferences.getLong(key, default)
    }

    override fun getFloat(key: String, default: Float): Float? {
        return sharedPreferences.getFloat(key, default)
    }

    override fun getSet(key: String, default: Set<String>): Set<String>? {
        return sharedPreferences.getStringSet(
            key,
            default,
        )
    }

    override fun contains(vararg keys: String): Boolean {
        return keys.all {
            sharedPreferences.contains(it)
        }
    }

    override fun saveUserCredentials(serverUrl: String, userName: String, pass: String) {
        setValue(SECURE_CREDENTIALS, true)
        setValue(SECURE_SERVER_URL, serverUrl)
        setValue(SECURE_USER_NAME, userName)
    }

    override fun updateServerURL(serverUrl: String) {
        sharedPreferences.edit().putString(SECURE_SERVER_URL, serverUrl).apply()
    }

    override fun areCredentialsSet(): Boolean {
        return getBoolean(SECURE_CREDENTIALS, false)
    }

    override fun areSameCredentials(serverUrl: String, userName: String, pass: String): Boolean {
        return getString(SECURE_SERVER_URL, "") == serverUrl &&
            getString(SECURE_USER_NAME, "") == userName
    }

    override fun saveJiraCredentials(jiraAuth: String): String {
        return String.format("Basic %s", jiraAuth)
    }

    override fun clear() {
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

    override fun lastMetadataSync(): Date? {
        return getString(LAST_META_SYNC)?.let { lastMetadataSyncString ->
            DateUtils.dateTimeFormat().parse(lastMetadataSyncString)
        }
    }

    override fun lastDataSync(): Date? {
        return getString(LAST_DATA_SYNC)?.let { lastDataSyncString ->
            DateUtils.dateTimeFormat().parse(lastDataSyncString)
        }
    }

    override fun lastSync(): Date? {
        return mutableListOf<Date>().apply {
            lastMetadataSync()?.let { add(it) }
            lastDataSync()?.let { add(it) }
        }.minOrNull()
    }

    /*endregion*/
}
