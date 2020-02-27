package org.dhis2.usescases.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.hamcrest.CoreMatchers.containsString
import org.hisp.dhis.android.core.mockwebserver.ResponseController.GET
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest : BaseTest() {

    /*@get:Rule
    val intentsTestRule = IntentsTestRule(LoginActivity::class.java)*/

    @get:Rule
    val ruleLogin = ActivityTestRule(LoginActivity::class.java, false, false)

    @get:Rule
    val mainRule = ActivityTestRule(MainActivity::class.java, false, false)

    override fun setUp() {
        super.setUp()
        setupMockServer()
    }

    @Test
    fun shouldLoginSuccessfullyWhenCredentialsAreRight() {
        mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_RESPONSE_OK)
        mockWebServerRobot.addResponse(GET, API_SYSTEM_INFO, API_SYSTEM_INFO_RESPONSE_OK)

        startLoginActivity()

        onView(withId(R.id.server_url_edit)).perform(clearText())
        onView(withId(R.id.server_url_edit)).perform(TypeTextAction(MOCK_SERVER_URL))
        onView(withId(R.id.user_name_edit)).perform(TypeTextAction("android"))
        onView(withId(R.id.user_pass_edit)).perform(TypeTextAction("Android123"))
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.login)).perform(click())
        onView(withId(R.id.dialogAccept)).perform(click())

        cleanDatabase()
    }

    @Test
    fun shouldGetAuthErrorWhenCredentialsAreWrong() {
        mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_UNAUTHORIZE, 401)

        startLoginActivity()

        onView(withId(R.id.server_url_edit)).perform(clearText())
        onView(withId(R.id.server_url_edit)).perform(TypeTextAction(MOCK_SERVER_URL))
        onView(withId(R.id.user_name_edit)).perform(TypeTextAction("android"))
        onView(withId(R.id.user_pass_edit)).perform(TypeTextAction("Android123"))
        onView(ViewMatchers.isRoot()).perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.login)).perform(click())
        onView(withId(R.id.dialogAccept)).perform(click())

        onView(withId(R.id.dialogTitle)).check(matches(withText(containsString(LOGIN_ERROR_TITLE))))
    }

    @Test
    fun shouldHideLoginButtonIfPasswordIsMissing() {
        val username = "android"
        val password = "Android123"
        startLoginActivity()

        loginRobot {
            typeServer(MOCK_SERVER_URL)
            typeUsername(username)
            typePassword(password)
            cleanPasswordField()
            checkLoginButtonIsHidden()
        }
    }

    @Test
    fun shouldLaunchWebViewWhenClickAccountRecoveryAndServerIsFilled() {
        startLoginActivity()

        loginRobot {
            typeServer(MOCK_SERVER_URL)
            clickAccountRecovery()
            //validate using intent if browser is launch
        }
    }

    fun startMainActivity(){
        mainRule.launchActivity(null)
    }

    fun startLoginActivity(){
        ruleLogin.launchActivity(null)
    }

    companion object {
        const val API_ME_PATH = "/api/me?.*"
        const val API_ME_RESPONSE_OK = "mocks/user/user.json"
        const val API_ME_UNAUTHORIZE = "mocks/user/unauthorize.json"
        const val API_SYSTEM_INFO = "/api/system/info?.*"
        const val API_SYSTEM_INFO_RESPONSE_OK = "mocks/systeminfo/systeminfo.json"

        const val LOGIN_ERROR_TITLE = "Login error"
    }
}
