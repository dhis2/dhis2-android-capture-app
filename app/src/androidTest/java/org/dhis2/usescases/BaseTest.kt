package org.dhis2.usescases

import android.content.Context
import android.os.Build
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.DisableAnimations
import org.dhis2.common.di.TestingInjector
import org.dhis2.common.keystore.KeyStoreRobot
import org.dhis2.common.keystore.KeyStoreRobot.Companion.KEYSTORE_PASSWORD
import org.dhis2.common.keystore.KeyStoreRobot.Companion.KEYSTORE_USERNAME
import org.dhis2.common.mockwebserver.MockWebServerRobot
import org.dhis2.common.preferences.PreferencesRobot
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
    }

    private fun allowPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
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
    }

    fun enableIntents() {
        if (!isIntentsEnable){
            Intents.init()
            isIntentsEnable = true
        }
    }

    fun setupCredentials() {
        val keyStoreRobot = TestingInjector.providesKeyStoreRobot(context)
        keyStoreRobot.apply {
            setData(KEYSTORE_USERNAME, "android")
            setData(KEYSTORE_PASSWORD, "Android123")
        }
    }

    private fun disableIntents() {
        if (isIntentsEnable){
            Intents.release()
            isIntentsEnable = false
        }
    }

    private fun cleanPreferences(){
        preferencesRobot.cleanPreferences()
    }

    private fun cleanKeystore() {
        keyStoreRobot.apply {
            removeData(KEYSTORE_USERNAME)
            removeData(KEYSTORE_PASSWORD)
        }
    }

    private fun stopMockServer(){
        mockWebServerRobot.shutdown()
    }

    companion object {
        @ClassRule
        @JvmField
        val disableAnimationsTestRule = DisableAnimations()
        const val MOCK_SERVER_URL = "http://127.0.0.1:8080"
    }
}