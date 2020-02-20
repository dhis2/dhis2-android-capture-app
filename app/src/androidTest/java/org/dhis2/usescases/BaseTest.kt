package org.dhis2.usescases

import android.content.Context
import android.os.Build
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.DisableAnimations
import org.dhis2.common.di.BaseTestComponent
import org.dhis2.common.di.TestingInjector
import org.dhis2.common.keystore.KeyStoreRobot
import org.dhis2.common.keystore.KeyStoreRobot.Companion.PASSWORD
import org.dhis2.common.keystore.KeyStoreRobot.Companion.USERNAME
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
    private lateinit var preferencesRobot: PreferencesRobot
    private lateinit var mockWebServerRobot: MockWebServerRobot


    protected open fun getPermissionsToBeAccepted() = arrayOf<String>()

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        injectDependencies()
        allowPermissions()
        forceLogInForUsingDB()
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
            mockWebServerRobot = providesMockWebserverRobot()
        }
    }

    fun setupMockServer() {
        mockWebServerRobot.start()
    }

    private fun forceLogInForUsingDB() {
    /*    keyStoreRobot.apply {
            setData(USERNAME,"android")
            setData(PASSWORD,"Android123")
        } */
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
            removeData(USERNAME)
            removeData(PASSWORD)
        }
    }

    private fun stopMockServer(){
        mockWebServerRobot.shutdown()
    }

    companion object {
        @ClassRule
        @JvmField
        val disableAnimationsTestRule = DisableAnimations()
    }
}