package org.dhis2.mobile.commons.providers

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import timber.log.Timber
import java.io.File

const val ENCRYPTED_SHARE_PREFS = "org.dhis2.enc"
private const val MIGRATION_MARKER = "__enc_migrated_from_plain"

/**
 * Returns a SharedPreferences backed by AES-256-GCM encryption with keys
 * anchored in the Android Keystore. On first use, any entries still sitting in
 * the legacy plaintext prefs file are copied over and the plaintext file is
 * cleared.
 *
 * On API < 23 the Keystore cannot back the master key, so we fall back to the
 * plain SharedPreferences (same behaviour as before this change). API 21-22
 * represents a negligible install base but still needs to not crash.
 */
fun createSecureSharedPreferences(
    context: Context,
    plainPrefsName: String,
): SharedPreferences {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return context.getSharedPreferences(plainPrefsName, Context.MODE_PRIVATE)
    }

    val encrypted = openEncryptedPrefs(context)
    migrateFromPlainIfNeeded(context, encrypted, plainPrefsName)
    return encrypted
}

private fun openEncryptedPrefs(context: Context): SharedPreferences {
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    return try {
        buildEncryptedPrefs(context, masterKeyAlias)
    } catch (e: Exception) {
        // Master key or file is unreadable (keystore invalidated, backup-restored
        // across devices, MAC mismatch). Wipe and recreate; any stored credentials
        // are lost and the user must re-authenticate.
        Timber.w(e, "Encrypted prefs unreadable; resetting")
        deleteSharedPreferencesFiles(context, ENCRYPTED_SHARE_PREFS)
        buildEncryptedPrefs(context, masterKeyAlias)
    }
}

private fun buildEncryptedPrefs(
    context: Context,
    masterKeyAlias: String,
): SharedPreferences = EncryptedSharedPreferences.create(
    ENCRYPTED_SHARE_PREFS,
    masterKeyAlias,
    context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
)

private fun migrateFromPlainIfNeeded(
    context: Context,
    encrypted: SharedPreferences,
    plainPrefsName: String,
) {
    if (encrypted.getBoolean(MIGRATION_MARKER, false)) return

    val plain = context.getSharedPreferences(plainPrefsName, Context.MODE_PRIVATE)
    val entries = plain.all
    encrypted.edit {
        for ((key, value) in entries) {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Set<*> -> {
                    if (value.all { it is String }) {
                        @Suppress("UNCHECKED_CAST")
                        putStringSet(key, value as Set<String>)
                    }
                }
            }
        }
        putBoolean(MIGRATION_MARKER, true)
    }
    if (entries.isNotEmpty()) {
        plain.edit { clear() }
    }
}

private fun deleteSharedPreferencesFiles(context: Context, name: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.deleteSharedPreferences(name)
        return
    }
    val dir = File(context.applicationInfo.dataDir, "shared_prefs")
    File(dir, "$name.xml").delete()
    File(dir, "$name.xml.bak").delete()
}
