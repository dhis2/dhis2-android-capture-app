package org.dhis2.data.sharedPreferences

import android.content.Context
import android.content.SharedPreferences


private const val SHARED_PREFS = "org.dhis2.shared_prefs"
private const val KEYSTORE_KEY_ALIAS = "Im9yZy5kaGlzMi5zaGFyZWRfcHJlZnMi"

class PreferencesRepository(val context: Context) {

    private fun String.encode(): String {
        return KeyStoreHelper.encrypt(KEYSTORE_KEY_ALIAS, this)
    }

    private fun String.decode(): String {
        return KeyStoreHelper.decrypt(KEYSTORE_KEY_ALIAS, this)
    }

    private val sp: SharedPreferences =
            context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)

    fun putInt(key: String, value: Int) {
        sp.edit().putString(key, value.toString().encode()).apply()
    }

    fun getInt(key: String, default: Int? = null): Int? {
        return try {
            val data = sp.getString(key, null)
            data?.let {
               return it.decode().toInt()
            }
            default
        } catch (e: Exception) {
            default
        }
    }

    fun putFloat(key: String, value: Float) {
        sp.edit().putString(key, value.toString().encode()).apply()
    }

    fun getFloat(key: String, default: Float? = null): Float? {
        return try {
            val data = sp.getString(key, null)
            data?.let {
                return it.decode().toFloat()
            }
            default
        } catch (e: Exception) {
            default
        }
    }

    fun putString(key: String, value: String?) {
        sp.edit().putString(key, value.toString().encode()).apply()
    }

    fun getString(key: String, default: String? = null): String? {
        return try {
            val data = sp.getString(key, null)
            data?.let {
                return it.decode()
            }
            default
        } catch (e: Exception) {
            default
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        sp.edit().putString(key, value.toString().encode()).apply()
    }

    fun getBoolean(key: String, default: Boolean? = null): Boolean? {
        try {
            val data = sp.getString(key, null)
            data?.let {
                return it.decode().toBoolean()
            }
            return default
        } catch (e: Exception) {
            return default
        }
    }

    fun remove(value: String) {
        sp.edit().remove(value).apply()
    }

    fun clear() {
        sp.edit().clear().apply()
    }

}
