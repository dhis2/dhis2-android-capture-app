package org.dhis2.commons.prefs

import com.google.gson.reflect.TypeToken
import org.dhis2.mobile.commons.biometrics.CiphertextWrapper
import java.util.Date

interface PreferenceProvider {
    fun saveUserCredentials(
        serverUrl: String,
        userName: String,
        pass: String?,
    )

    fun saveUserCredentialsAndCipher(
        serverUrl: String,
        userName: String,
        ciphertextWrapper: CiphertextWrapper,
    )

    fun getBiometricCredentials(): CiphertextWrapper?

    fun areCredentialsSet(): Boolean

    fun areSameCredentials(
        serverUrl: String?,
        userName: String?,
    ): Boolean

    fun clear()

    fun setValue(
        key: String,
        value: Any? = null,
    )

    fun secureValue(
        key: String,
        value: Any? = null,
    )

    fun getSecureValue(
        key: String,
        default: String? = null,
    ): String?

    fun removeValue(key: String)

    fun contains(vararg keys: String): Boolean

    fun getString(
        key: String,
        default: String? = null,
    ): String?

    fun getInt(
        key: String,
        default: Int,
    ): Int

    fun getLong(
        key: String,
        default: Long,
    ): Long?

    fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean

    fun getFloat(
        key: String,
        default: Float,
    ): Float?

    fun getSet(
        key: String,
        default: Set<String>,
    ): Set<String>?

    fun <T> getObjectFromJson(
        key: String,
        typeToken: TypeToken<T>,
        default: T,
    ): T

    fun <T> saveAsJson(
        key: String,
        objectToSave: T,
    )

    fun lastMetadataSync(): Date?

    fun lastDataSync(): Date?

    fun lastSync(): Date?

    fun getList(
        key: String,
        default: List<String>,
    ): List<String>
}
