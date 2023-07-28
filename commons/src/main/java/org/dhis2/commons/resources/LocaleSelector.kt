package org.dhis2.commons.resources

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale
import org.hisp.dhis.android.core.D2

private const val OVERRIDE_LANGUAGE_KEY = "OVERRIDE_LANGUAGE_KEY"

class LocaleSelector(private val base: Context, private val d2: D2) {

    private val resources: Resources = base.resources
    private val configuration: Configuration = resources.configuration

    fun updateUiLanguage(): ContextWrapper {
        return ContextWrapper(changeToUserLanguage(base))
    }

    fun overrideUserLanguage(locale: Locale) {
        if (locale.language == getUserLanguage()) {
            d2.dataStoreModule().localDataStore()
                .value(OVERRIDE_LANGUAGE_KEY)
                .blockingDeleteIfExist()
        } else {
            d2.dataStoreModule().localDataStore()
                .value(OVERRIDE_LANGUAGE_KEY)
                .blockingSet(locale.language)
        }
    }

    fun getUserLanguage(): String? {
        return overridenUserLanguage() ?: d2.settingModule().userSettings().blockingGet()
            ?.keyUiLocale()
    }

    private fun overridenUserLanguage(): String? {
        return d2.dataStoreModule().localDataStore()
            .value(OVERRIDE_LANGUAGE_KEY)
            .blockingGet()?.value()
    }

    private fun hasLanguageChanged(): Boolean {
        val userLanguage = getUserLanguage()
        val currentLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0].toLanguageTag()
        } else {
            configuration.locale.language
        }
        return userLanguage != null && userLanguage != currentLanguage
    }

    private fun changeToUserLanguage(base: Context): Context {
        var context = base
        if (!d2.userModule().isLogged().blockingGet()) {
            return context
        }
        getUserLanguage()?.let { newLocale ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(Locale(newLocale.toLowerCase()))
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                configuration.locale = Locale(newLocale.toLowerCase())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context = context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
        }
        return context
    }
}
