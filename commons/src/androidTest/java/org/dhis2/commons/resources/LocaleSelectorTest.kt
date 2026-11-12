package org.dhis2.commons.resources

import android.content.Context
import android.content.res.Resources
import androidx.test.platform.app.InstrumentationRegistry
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.configuration.internal.DatabaseAccount
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Date

class LocaleSelectorTest {

    lateinit var localeSelector: LocaleSelector
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setUp() {
        context.getSharedPreferences("locale_selector", Context.MODE_PRIVATE).edit().clear()
            .commit()
        localeSelector = LocaleSelector(context, d2)
    }

    @Test
    fun useCachedLanguageInsteadOfReadingTheDatabase() {
        d2.loggedInto("account_a")
        d2.storedLanguage("es")
        localeSelector.updateUiLanguage()

        // The database now holds a different language; the cached one must be used, and the
        // database only read by the deferred background refresh.
        val pendingRefreshes = mutableListOf<Runnable>()
        val updatedD2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
        updatedD2.loggedInto("account_a")
        updatedD2.storedLanguage("fr")

        val wrapped =
            LocaleSelector(context, updatedD2) { pendingRefreshes.add(it) }.updateUiLanguage()

        assert(wrapped.resources.configuration.locales[0].language == "es")
        assert(pendingRefreshes.size == 1)

        pendingRefreshes.forEach { it.run() }
        LocaleSelector(context, updatedD2) { pendingRefreshes.add(it) }.updateUiLanguage()
        assert(context.resources.configuration.locales[0].language == "fr")
        // Let the last refresh finish so the shared in-progress flag is released.
        pendingRefreshes.last().run()
    }

    @Test
    fun keepEachAccountLanguageWhenSwitchingAccounts() {
        val accountA: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
        accountA.loggedInto("account_a")
        accountA.storedLanguage("es")
        LocaleSelector(context, accountA).updateUiLanguage()

        val accountB: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
        accountB.loggedInto("account_b")
        accountB.storedLanguage("fr")
        LocaleSelector(context, accountB).updateUiLanguage()
        assert(context.resources.configuration.locales[0].language == "fr")

        // Back to account A, with no sync in between: its own cached language, not account B's.
        val pendingRefreshes = mutableListOf<Runnable>()
        val backToA =
            LocaleSelector(context, accountA) { pendingRefreshes.add(it) }.updateUiLanguage()
        assert(backToA.resources.configuration.locales[0].language == "es")
        pendingRefreshes.forEach { it.run() }
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
        d2.loggedInto("account_a")
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

    private fun D2.loggedInto(databaseName: String) {
        whenever(userModule().blockingIsLogged()) doReturn true
        whenever(userModule().accountManager().getCurrentAccount()) doReturn
                DatabaseAccount(
                    username = "user",
                    serverUrl = "https://server.org",
                    databaseName = databaseName,
                    databaseCreationDate = Date(),
                    lastAccessDate = null,
                    encrypted = false,
                    syncState = null,
                    importDB = null,
                    loginConfig = null,
                    authorizationType = null,
                )
    }

    private fun D2.storedLanguage(language: String) {
        whenever(
            dataStoreModule().localDataStore().value("OVERRIDE_LANGUAGE_KEY").blockingGet()
                ?.value(),
        ) doReturn language
    }
}
