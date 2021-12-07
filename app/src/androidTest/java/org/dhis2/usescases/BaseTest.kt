package org.dhis2.usescases

import android.content.Context
import android.os.Build
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.AppTest
import org.dhis2.AppTest.Companion.DB_TO_IMPORT
import org.dhis2.common.di.TestingInjector
import org.dhis2.common.keystore.KeyStoreRobot
import org.dhis2.common.keystore.KeyStoreRobot.Companion.KEYSTORE_PASSWORD
import org.dhis2.common.keystore.KeyStoreRobot.Companion.KEYSTORE_USERNAME
import org.dhis2.common.keystore.KeyStoreRobot.Companion.PASSWORD
import org.dhis2.common.keystore.KeyStoreRobot.Companion.USERNAME
import org.dhis2.common.mockwebserver.MockWebServerRobot
import org.dhis2.common.preferences.PreferencesRobot
import org.dhis2.common.rules.DisableAnimations
import org.dhis2.commons.idlingresource.CountingIdlingResourceSingleton
import org.dhis2.commons.prefs.Preference
import org.dhis2.form.ui.idling.FormCountingIdlingResource
import org.hisp.dhis.android.core.arch.api.internal.ServerURLWrapper
import org.junit.After
import org.junit.Before
import org.junit.ClassRule

open class BaseTest {

    @JvmField
    protected var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var isIntentsEnable = false
    private lateinit var keyStoreRobot: KeyStoreRobot
    lateinit var preferencesRobot: PreferencesRobot
    lateinit var mockWebServerRobot: MockWebServerRobot

    protected open fun getPermissionsToBeAccepted() = arrayOf<String>()

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        injectDependencies()
        allowPermissions()
        registerCountingIdlingResource()
    }

    private fun allowPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionsToBeAccepted().forEach {
                InstrumentationRegistry.getInstrumentation()
                    .uiAutomation
                    .executeShellCommand("pm grant ${context.packageName} $it")
            }
        }
    }

    private fun injectDependencies() {
        TestingInjector.apply {
            keyStoreRobot = providesKeyStoreRobot(context)
            preferencesRobot = providesPreferencesRobot(context)
            mockWebServerRobot = providesMockWebserverRobot(context)
        }
    }

    private fun registerCountingIdlingResource() {
        IdlingRegistry.getInstance().register(
            CountingIdlingResourceSingleton.countingIdlingResource,
            FormCountingIdlingResource.countingIdlingResource
        )
    }

    private fun unregisterCountingIdlingResource() {
        IdlingRegistry.getInstance()
            .unregister(
                CountingIdlingResourceSingleton.countingIdlingResource,
                FormCountingIdlingResource.countingIdlingResource
            )
    }

    fun setupMockServer() {
        mockWebServerRobot.start()
    }

    @After
    @Throws(Exception::class)
    open fun teardown() {
        disableIntents()
        cleanPreferences()
        cleanKeystore()
        stopMockServer()
        unregisterCountingIdlingResource()
    }

    fun enableIntents() {
        if (!isIntentsEnable) {
            Intents.init()
            isIntentsEnable = true
        }
    }

    fun setupCredentials() {
        val keyStoreRobot = TestingInjector.providesKeyStoreRobot(context)
        keyStoreRobot.apply {
            setData(KEYSTORE_USERNAME, USERNAME)
            setData(KEYSTORE_PASSWORD, PASSWORD)
        }
    }

    fun setDatePicker() {
        preferencesRobot.saveValue(Preference.DATE_PICKER, true)
    }

    fun turnOnConnectivityAfterLogin() {
        ServerURLWrapper.setServerUrl("$MOCK_SERVER_URL/$API/")
    }

    fun turnOffConnectivityAfterLogin() {
        ServerURLWrapper.setServerUrl("none")
    }

    private fun disableIntents() {
        if (isIntentsEnable) {
            Intents.release()
            isIntentsEnable = false
        }
    }

    private fun cleanPreferences() {
        preferencesRobot.cleanPreferences()
    }

    private fun cleanKeystore() {
        keyStoreRobot.apply {
            removeData(KEYSTORE_USERNAME)
            removeData(KEYSTORE_PASSWORD)
        }
    }

    private fun stopMockServer() {
        mockWebServerRobot.shutdown()
    }

    fun cleanLocalDatabase() {
        (context.applicationContext as AppTest).deleteDatabase(DB_TO_IMPORT)
    }

    companion object {
        @ClassRule
        @JvmField
        val disableAnimationsTestRule = DisableAnimations()
        const val MOCK_SERVER_URL = "http://127.0.0.1:8080"
        const val API = "api"
    }
}
