package org.dhis2.usescases

import android.content.Context
import android.os.Build
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.DisableAnimations
import org.dhis2.common.KeyStoreRobot
import org.dhis2.common.KeyStoreRobot.Companion.PASSWORD
import org.dhis2.common.KeyStoreRobot.Companion.USERNAME
import org.junit.After
import org.junit.Before
import org.junit.ClassRule

open class BaseTest {

    @JvmField
    protected var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var isIntentsEnable = false
    private lateinit var keyStoreRobot: KeyStoreRobot

    protected open fun getPermissionsToBeAccepted() = arrayOf<String>()

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        injectDependencies()
        allowPermissions()
        setupMockServerIfNeeded()
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
      //  keyStoreRobot = TestingInjector.createKeyStoreRobot(context)
    }

    private fun setupMockServerIfNeeded() {

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
    //    cleanPreferences()
//      cleanKeystore()
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

    private fun cleanPreferences() {
        keyStoreRobot.apply {
            removeData(USERNAME)
            removeData(PASSWORD)
        }
    }

    companion object {
        @ClassRule
        @JvmField
        val disableAnimationsTestRule = DisableAnimations()
    }
}