package org.dhis2.commons.resources

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import org.dhis2.mobile.commons.resources.UserLanguageCache
import org.dhis2.mobile.commons.resources.UserLanguageCache.Companion.OVERRIDE_LANGUAGE_KEY
import org.hisp.dhis.android.core.D2
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class LocaleSelector(
    private val base: Context,
    private val d2: D2,
    private val refreshExecutor: Executor = defaultRefreshExecutor,
) {
    private val resources: Resources = base.resources
    private val configuration: Configuration = resources.configuration
    private val deviceConfig: Configuration = Resources.getSystem().configuration
    private val languageCache = UserLanguageCache(base, d2)

    fun updateUiLanguage(): ContextWrapper = ContextWrapper(changeToUserLanguage(base))

    fun overrideUserLanguage(locale: Locale) {
        if (locale.language == getUserLanguage()) {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(OVERRIDE_LANGUAGE_KEY)
                .blockingDeleteIfExist()
        } else {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(OVERRIDE_LANGUAGE_KEY)
                .blockingSet(locale.language)
        }
        languageCache.refresh()
    }

    fun getUserLanguage(): String? = languageCache.userLanguageFromDatabase()

    private fun changeToUserLanguage(base: Context): Context {
        var context = base
        if (!d2.userModule().blockingIsLogged()) {
            setDefaultLocale()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context = context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
        } else {
            cachedUserLanguage()?.let { newLocale ->
                setDefaultLocale(newLocale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    context = context.createConfigurationContext(configuration)
                } else {
                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
            }
        }
        return context
    }

    /**
     * Language for the UI without touching the database on a cache hit.
     *
     * Called from attachBaseContext on the main thread, where a database read can wait on a
     * running sync until Android raises an ANR. The cache is refreshed after every metadata sync
     * and language override; the background refresh here only covers any other change.
     * The cache is kept per account, so switching back to an account uses its own language.
     * Only a cache miss (the account's first screen on this version) reads the database here.
     */
    private fun cachedUserLanguage(): String? {
        val accountKey = languageCache.currentAccountKey() ?: return getUserLanguage()
        return if (languageCache.isCached(accountKey)) {
            refreshCacheInBackground()
            languageCache.cachedLanguage(accountKey)
        } else {
            languageCache.refresh()
        }
    }

    private fun refreshCacheInBackground() {
        if (!refreshInProgress.compareAndSet(false, true)) return
        refreshExecutor.execute {
            try {
                languageCache.refresh()
            } catch (e: Exception) {
                // The database may not be available (e.g. after logout); keep the last value.
                Timber.d(e, "Could not refresh the cached user language")
            } finally {
                refreshInProgress.set(false)
            }
        }
    }

    private fun setDefaultLocale(newLocale: String? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locale =
                newLocale?.lowercase(Locale.getDefault())
                    ?: deviceConfig.locales[0].language
            val localeList = LocaleList(Locale(locale))
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            val locale =
                newLocale?.lowercase(Locale.getDefault())
                    ?: deviceConfig.locale.language
            configuration.locale = Locale(locale)
        }
    }

    private companion object {
        val defaultRefreshExecutor: Executor = Executors.newSingleThreadExecutor()
        val refreshInProgress = AtomicBoolean(false)
    }
}
