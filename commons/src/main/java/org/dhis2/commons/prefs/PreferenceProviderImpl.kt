package org.dhis2.commons.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.dhis2.commons.date.DateUtils
import org.dhis2.mobile.commons.biometrics.CiphertextWrapper
import org.dhis2.mobile.commons.providers.BIOMETRIC_CREDENTIALS
import org.dhis2.mobile.commons.providers.LAST_DATA_SYNC
import org.dhis2.mobile.commons.providers.LAST_META_SYNC
import org.dhis2.mobile.commons.providers.SECURE_CREDENTIALS
import org.dhis2.mobile.commons.providers.SECURE_PASS
import org.dhis2.mobile.commons.providers.SECURE_SERVER_URL
import org.dhis2.mobile.commons.providers.SECURE_USER_NAME
import org.dhis2.mobile.commons.providers.SHARE_PREFS
import org.hisp.dhis.android.core.arch.storage.internal.AndroidSecureStore
import org.hisp.dhis.android.core.arch.storage.internal.ChunkedSecureStore
import timber.log.Timber
import java.util.Date

open class PreferenceProviderImpl(
    context: Context,
) : PreferenceProvider {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARE_PREFS, Context.MODE_PRIVATE)

    private val asc = AndroidSecureStore(context)
    private val css = ChunkedSecureStore(asc)

    init {
        sharedPreferences.edit { remove(SECURE_PASS) }
    }

    override fun getSecureValue(
        key: String,
        default: String?,
    ): String? = css.getData(key) ?: default

    override fun secureValue(
        key: String,
        value: Any?,
    ) {
        if (value is String) {
            css.setData(key, value)
        } else {
            setValue(key, value)
        }
    }

    override fun setValue(
        key: String,
        value: Any?,
    ) {
        value?.let {
            when (it) {
                is String -> {
                    sharedPreferences.edit { putString(key, it) }
                }

                is Boolean -> {
                    sharedPreferences.edit { putBoolean(key, it) }
                }

                is Int -> {
                    sharedPreferences.edit { putInt(key, it) }
                }

                is Long -> {
                    sharedPreferences.edit { putLong(key, it) }
                }

                is Float -> {
                    sharedPreferences.edit { putFloat(key, it) }
                }

                is Set<*> -> {
                    if (it.all { element -> element is String }) {
                        @Suppress("UNCHECKED_CAST")
                        val stringSet = it as Set<String>
                        sharedPreferences.edit { putStringSet(key, stringSet) }
                    } else {
                        Timber.e("Attempted to save a Set for key '$key' that does not exclusively contain Strings. Skipping.")
                    }
                }
            }
        } ?: removeValue(key)
    }

    override fun removeValue(key: String) {
        if (contains(key)) {
            sharedPreferences.edit { remove(key) }
        }
    }

    override fun getString(
        key: String,
        default: String?,
    ): String? = sharedPreferences.getString(key, default)

    override fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean = sharedPreferences.getBoolean(key, default)

    override fun getInt(
        key: String,
        default: Int,
    ): Int = sharedPreferences.getInt(key, default)

    override fun getLong(
        key: String,
        default: Long,
    ): Long? = sharedPreferences.getLong(key, default)

    override fun getFloat(
        key: String,
        default: Float,
    ): Float? = sharedPreferences.getFloat(key, default)

    override fun getSet(
        key: String,
        default: Set<String>,
    ): Set<String>? =
        sharedPreferences.getStringSet(
            key,
            default,
        )

    override fun getList(
        key: String,
        default: List<String>,
    ): List<String> {
        val json = getString(key, null)
        return json?.let {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, type)
        } ?: default
    }

    override fun contains(vararg keys: String): Boolean =
        keys.all { key ->
            sharedPreferences.contains(key) or
                css
                    .getAllKeys()
                    .any { storedKey -> storedKey.startsWith(key) }
        }

    override fun saveUserCredentials(
        serverUrl: String,
        userName: String,
        pass: String?,
    ) {
        setValue(SECURE_CREDENTIALS, true)
        setValue(SECURE_SERVER_URL, serverUrl)
        setValue(SECURE_USER_NAME, userName)
        pass?.let { secureValue(SECURE_PASS, it) }
    }

    override fun saveUserCredentialsAndCipher(
        serverUrl: String,
        userName: String,
        ciphertextWrapper: CiphertextWrapper,
    ) {
        saveUserCredentials(serverUrl, userName, null)
        saveAsJson(BIOMETRIC_CREDENTIALS, ciphertextWrapper)
    }

    override fun getBiometricCredentials(): CiphertextWrapper? =
        getObjectFromJson(
            BIOMETRIC_CREDENTIALS,
            object : TypeToken<CiphertextWrapper?>() {},
            null,
        )

    override fun areCredentialsSet(): Boolean = getBoolean(SECURE_CREDENTIALS, false)

    override fun areSameCredentials(
        serverUrl: String?,
        userName: String?,
    ): Boolean =
        getString(SECURE_SERVER_URL, "") == serverUrl &&
            getString(SECURE_USER_NAME, "") == userName

    override fun clear() {
        sharedPreferences.edit { clear() }
    }

    override fun <T> saveAsJson(
        key: String,
        objectToSave: T,
    ) {
        setValue(key, Gson().toJson(objectToSave))
    }

    override fun <T> getObjectFromJson(
        key: String,
        typeToken: TypeToken<T>,
        default: T,
    ): T {
        val mapTypeToken = typeToken.type
        return if (getString(key, null) == null) {
            default
        } else {
            Gson().fromJson<T>(getString(key), mapTypeToken)
        }
    }

    override fun lastMetadataSync(): Date? =
        getString(LAST_META_SYNC)?.let { lastMetadataSyncString ->
            DateUtils.dateTimeFormat().parse(lastMetadataSyncString)
        }

    override fun lastDataSync(): Date? =
        getString(LAST_DATA_SYNC)?.let { lastDataSyncString ->
            DateUtils.dateTimeFormat().parse(lastDataSyncString)
        }

    override fun lastSync(): Date? =
        mutableListOf<Date>()
            .apply {
                lastMetadataSync()?.let { add(it) }
                lastDataSync()?.let { add(it) }
            }.minOrNull()

    // endregion
}
