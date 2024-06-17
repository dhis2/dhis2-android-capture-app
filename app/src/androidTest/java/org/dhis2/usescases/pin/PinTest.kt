package org.dhis2.usescases.pin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.commons.prefs.Preference.Companion.SESSION_LOCKED
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

    @Test
    fun openPin() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnPin()
        }
    }

//    @Ignore("Killing process makes test failed")
    @Test
    fun shouldCloseAppIfPinIsSet() = runTest {
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
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            withContext(Dispatchers.Main) {
                pinRobot {
                    checkActivityHasFinished(ruleLoginActivity.activity)
                }
            }
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

    fun startActivity() {
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
