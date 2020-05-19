package org.dhis2.usescases.pin

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.data.prefs.Preference.Companion.PIN
import org.dhis2.data.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PinTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    @get:Rule
    val ruleLoginActivity = ActivityTestRule(LoginActivity::class.java, false, false)

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    }

    @Test
    fun openPin() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnPin()
        }
    }

    @Test
    @Ignore("Killing process makes test failed")
    fun shouldCloseAppIfPinIsSet() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnPin()
        }

        pinRobot {
            clickPinButton("1")
            clickPinButton("2")
            clickPinButton("3")
            clickPinButton("4")
        }
    }

    @Test
    fun shouldRedirectToHomeIfPinIsCorrect() {
        enableIntents()
        preferencesRobot.saveValue(SESSION_LOCKED, true)
        preferencesRobot.saveValue(PIN, PIN_NUMBER)
        startLoginActivity()

        pinRobot {
            clickPinButton("1")
            clickPinButton("2")
            clickPinButton("3")
            clickPinButton("4")
            checkRedirectToHome()
        }
    }

    @Test
    fun shouldSendErrorIfPinIsWrong() {
        preferencesRobot.saveValue(SESSION_LOCKED, true)
        preferencesRobot.saveValue(PIN, PIN_NUMBER)
        startLoginActivity()

        pinRobot {
            clickPinButton("1")
            clickPinButton("2")
            clickPinButton("3")
            clickPinButton("3")
            checkToastDisplayed(PIN_ERROR)
        }
    }

    @Test
    fun shouldSuccessfullyLoginIfClickForgotYourCode() {
        enableIntents()
        preferencesRobot.saveValue(SESSION_LOCKED, true)
        preferencesRobot.saveValue(PIN, PIN_NUMBER)
        startLoginActivity()

        pinRobot {
            clickForgotCode()
        }

        homeRobot {
            checkLogInIsLaunched()
        }
    }

    fun startActivity(){
        rule.launchActivity(null)
    }

    fun startLoginActivity() {
        ruleLoginActivity.launchActivity(null)
    }

    companion object {
        const val PIN_NUMBER = "1234"
        const val PIN_ERROR = "Wrong pin"
    }
}