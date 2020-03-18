package org.dhis2.usescases.pin

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.common.matchers.isToast
import org.dhis2.data.prefs.Preference.Companion.PIN
import org.dhis2.data.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.login.LoginActivity
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

    @get:Rule
    val ruleLoginActivity = ActivityTestRule(LoginActivity::class.java, false, false)

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    }

    @Test
    fun openPin() {
        /*startActivity()
        mainRobot.clickOnNavigationDrawerMenu()
                .clickOnPin()*/
    }

    @Test
    fun shouldAppClosedIfSetPin() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnPin()
        }
        onView(withText("1")).perform(click())
        onView(withText("2")).perform(click())
        onView(withText("3")).perform(click())
        onView(withText("4")).perform(click())
       // Thread.sleep(1000)
        /*preferencesRobot.saveValue(PIN, "1234")
        preferencesRobot.saveValue(SESSION_LOCKED, true)*/
    }

    @Test
    fun shouldRedirectToHomeIfPinIsCorrect() {
        //prefrenesRobot.ssaveValue(PIN, "12342)
        //prefrencesRobot.saveValue(Session_Locked, true)
        //Si escribo 1234 voy a la home
        //Si escribo 4123 no voy a ninguno sitio . Check Pin incorrecto

        preferencesRobot.saveValue(SESSION_LOCKED, true)
        preferencesRobot.saveValue(PIN, "1234")
        startLoginActivity()
        onView(withText("1")).perform(click())
        onView(withText("2")).perform(click())
        onView(withText("3")).perform(click())
        onView(withText("4")).perform(click())
        // inteded home activity
    }

    @Test
    fun shouldSendErrorIfPinIsWrong() {

        //Type 4321
        //should show warning

        preferencesRobot.saveValue(SESSION_LOCKED, true)
        preferencesRobot.saveValue(PIN, "1234")
        startLoginActivity()
        onView(withText("1")).perform(click())
        onView(withText("2")).perform(click())
        onView(withText("3")).perform(click())
        onView(withText("3")).perform(click())

        //check how to match toast
     //   onView(withText("Wrong pin")).check(matches(isDisplayed()))
        //onView(withText("Wrong pin")).inRoot(withDecorView(not(`is`(ruleLoginActivity.activity.window.decorView)))).check(matches(isDisplayed()))
        onView(withText("Wrong pin")).inRoot(isToast()).check(matches(isDisplayed()))
        Thread.sleep(1000)
    }

    @Test
    fun shouldSuccessfullyLoginIfClickForgotYourCode() {

        preferencesRobot.saveValue(SESSION_LOCKED, true)
        preferencesRobot.saveValue(PIN, "1234")
        startLoginActivity()

        pinRobot {
            clickForgotCode()
        }
        //intended al login
    }

    fun startActivity(){
        rule.launchActivity(null)
    }

    fun startLoginActivity() {
        ruleLoginActivity.launchActivity(null)
    }
}