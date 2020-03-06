package org.dhis2.usescases.pin

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.data.prefs.Preference.Companion.PIN
import org.dhis2.data.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainRobot
import org.dhis2.usescases.main.homeRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinTest : BaseTest() {

    private lateinit var mainRobot: MainRobot

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    }

    @Test
    fun openPin() {
        startActivity()
        mainRobot.clickOnNavigationDrawerMenu()
                .clickOnPin()
    }

    @Test
    fun shouldAppClosedIfSetPin() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnPin()
        }
        preferencesRobot.saveValue(PIN, "1234")
        preferencesRobot.saveValue(SESSION_LOCKED, true)
    }

    @Test
    fun shouldRedirectToHomeIfPinIsCorrect() {
        //prefrenesRobot.ssaveValue(PIN, "12342)
        //prefrencesRobot.saveValue(Session_Locked, true)
        //Si escribo 1234 voy a la home
        //Si escribo 4123 no voy a ninguno sitio . Check Pin incorrecto
    }

    @Test
    fun shouldSendErrorIfPinIsWrong() {

    }

    @Test
    fun shouldSuccessfullyLoginIfClickForgotYourCode() {
        //startActivity()

    }

    fun startActivity(){
        rule.launchActivity(null)
    }
}