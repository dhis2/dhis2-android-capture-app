package org.dhis2.mobile.commons.resources

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.hisp.dhis.android.core.D2

private const val LANGUAGE_CACHE_PREFS = "locale_selector"
private const val CACHED_LANGUAGE_KEY_PREFIX = "CACHED_USER_LANGUAGE_"
private const val NO_LANGUAGE = ""

class UserLanguageCache(
    context: Context,
    private val d2: D2,
) {
    private val preferences: SharedPreferences =
        (context.applicationContext ?: context)
            .getSharedPreferences(LANGUAGE_CACHE_PREFS, Context.MODE_PRIVATE)

    fun currentAccountKey(): String? =
        d2
            .userModule()
            .accountManager()
            .getCurrentAccount()
            ?.databaseName
            ?.let { CACHED_LANGUAGE_KEY_PREFIX + it }

    fun isCached(accountKey: String): Boolean = preferences.contains(accountKey)

    fun cachedLanguage(accountKey: String): String? =
        preferences
            .getString(accountKey, NO_LANGUAGE)
            ?.takeIf { it != NO_LANGUAGE }

    fun refresh(): String? {
        val accountKey = currentAccountKey()
        return userLanguageFromDatabase().also { language ->
            accountKey?.let { store(it, language) }
        }
    }

    fun userLanguageFromDatabase(): String? =
        d2
            .dataStoreModule()
            .localDataStore()
            .value(OVERRIDE_LANGUAGE_KEY)
            .blockingGet()
            ?.value()
            ?: d2
                .settingModule()
                .userSettings()
                .blockingGet()
                ?.keyUiLocale()

    private fun store(
        accountKey: String,
        language: String?,
    ) {
        preferences.edit { putString(accountKey, language ?: NO_LANGUAGE) }
    }

    companion object {
        const val OVERRIDE_LANGUAGE_KEY = "OVERRIDE_LANGUAGE_KEY"
    }
}
