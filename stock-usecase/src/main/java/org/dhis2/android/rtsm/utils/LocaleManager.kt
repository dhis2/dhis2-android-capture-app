package org.dhis2.android.rtsm.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.preference.PreferenceManager
import org.dhis2.android.rtsm.R
import java.util.Locale

class LocaleManager {
    companion object {
        @JvmStatic
        fun setLocale(context: Context): Context = updateResources(context, getPreferredLanguage(context))

        @JvmStatic
        fun getPreferredLanguage(context: Context): String {
            val prefKey = context.getString(R.string.language_pref_key)
            val defaultValue = context.getString(R.string.language_pref_default)

            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(prefKey, defaultValue)!!
        }

        private fun updateResources(
            context: Context,
            language: String,
        ): Context {
            val config = updateConfiguration(context, language)

            return context.createConfigurationContext(config)
        }

        @JvmStatic
        fun updateConfiguration(
            context: Context,
            language: String,
        ): Configuration {
            val newLocale = Locale(language)
            val resources = context.resources
            val config = Configuration(resources.configuration)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                config.setLocales(localeList) // additionally sets the layout direction
            } else {
                config.locale = newLocale
                resources.updateConfiguration(config, resources.displayMetrics)
            }

            return config
        }
    }
}
