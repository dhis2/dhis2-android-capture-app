package org.dhis2.android.rtsm.services.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import org.dhis2.android.rtsm.commons.Constants

class PreferenceProviderImpl(context: Context): PreferenceProvider {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        Constants.SHARED_PREFS, Context.MODE_PRIVATE)

    companion object {
        private var INSTANCE: PreferenceProvider? = null

        @JvmStatic
        fun getInstance(context: Context): PreferenceProvider =
            INSTANCE ?: PreferenceProviderImpl(context)
    }

    override fun sharedPreferences() = sharedPreferences

    override fun saveUserCredentials(serverUrl: String, userName: String, password: String) {
        with(sharedPreferences.edit()) {
            putString(Constants.SERVER_URL, serverUrl)
            putString(Constants.USERNAME, userName)

            if (password.isNotEmpty())
                putString(Constants.PASSWORD, password)

            apply()
        }
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    override fun setValue(key: String, value: Any?) {
        value?.let {
            when(it) {
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
                else -> return
            }
        } ?: run {
            sharedPreferences.edit().remove(key).apply()
        }
    }

    override fun removeValue(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override fun contains(vararg keys: String): Boolean {
        return keys.all {
            sharedPreferences.contains(it)
        }
    }

    override fun getString(key: String, default: String?) =
        sharedPreferences.getString(key, default)

    override fun getStringSet(key: String, defValues: Set<String>?) =
        sharedPreferences.getStringSet(key, defValues)

    override fun getInt(key: String, default: Int) =
        sharedPreferences.getInt(key, default)

    override fun getLong(key: String, default: Long) =
        sharedPreferences.getLong(key, default)

    override fun getBoolean(key: String, default: Boolean) =
        sharedPreferences.getBoolean(key, default)

    override fun getFloat(key: String, default: Float) =
        sharedPreferences.getFloat(key, default)

    override fun preferenceDataStore(context: Context) =
        object: PreferenceDataStore() {
            override fun putString(key: String, value: String?) {
                getInstance(context).setValue(key, value)
            }

            override fun putStringSet(key: String, values: Set<String>?) {
                getInstance(context).setValue(key, values)
            }

            override fun putInt(key: String, value: Int) {
                getInstance(context).setValue(key, value)
            }

            override fun putLong(key: String, value: Long) {
                getInstance(context).setValue(key, value)
            }

            override fun putFloat(key: String, value: Float) {
                getInstance(context).setValue(key, value)
            }

            override fun putBoolean(key: String, value: Boolean) {
                getInstance(context).setValue(key, value)
            }

            override fun getString(key: String, defValue: String?) =
                getInstance(context).getString(key, defValue)

            override fun getStringSet(
                key: String,
                defValues: Set<String>?
            ) = getInstance(context).getStringSet(key, defValues)

            override fun getInt(key: String, defValue: Int) =
                getInstance(context).getInt(key, defValue)

            override fun getLong(key: String, defValue: Long) =
                getInstance(context).getLong(key, defValue)

            override fun getFloat(key: String, defValue: Float) =
                getInstance(context).getFloat(key, defValue)

            override fun getBoolean(key: String, defValue: Boolean) =
                getInstance(context).getBoolean(key, defValue)
        }
}