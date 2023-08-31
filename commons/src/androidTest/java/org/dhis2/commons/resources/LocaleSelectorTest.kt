package org.dhis2.commons.resources

import android.content.Context
import android.content.res.Resources
import androidx.test.platform.app.InstrumentationRegistry
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class LocaleSelectorTest {

    lateinit var localeSelector: LocaleSelector
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setUp() {
        localeSelector = LocaleSelector(context, d2)
    }

    @Test
    fun setDefaultLanguageWhenNotLogged() {
        val deviceLanguage = Resources.getSystem().configuration.locales[0].language
        whenever(d2.userModule().blockingIsLogged()) doReturn false
        localeSelector.updateUiLanguage()
        assert(context.resources.configuration.locales[0].language == deviceLanguage)
    }

    @Test
    fun setServerLanguage() {
        val language = "en"
        whenever(d2.userModule().blockingIsLogged()) doReturn true
        whenever(
            d2.dataStoreModule().localDataStore().value("OVERRIDE_LANGUAGE_KEY").blockingGet()
                ?.value()
        ) doReturn language
        localeSelector.updateUiLanguage()
        assert(context.resources.configuration.locales[0].language == language)
    }

    @Test
    fun getUserLanguageFromOverride() {
        val language = "fr"
        whenever(
            d2.dataStoreModule().localDataStore().value("OVERRIDE_LANGUAGE_KEY").blockingGet()
                ?.value()
        ) doReturn language
        assert(localeSelector.getUserLanguage() == language)
    }

    @Test
    fun getUserLanguageFromSettings() {
        val language = "fr"
        whenever(
            d2.dataStoreModule().localDataStore().value("OVERRIDE_LANGUAGE_KEY").blockingGet()
                ?.value()
        ) doReturn null
        whenever(d2.settingModule().userSettings().blockingGet()?.keyUiLocale()) doReturn language
        assert(localeSelector.getUserLanguage() == language)
    }
}