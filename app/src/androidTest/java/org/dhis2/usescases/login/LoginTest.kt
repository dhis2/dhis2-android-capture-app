package org.dhis2.usescases.login

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.hisp.dhis.android.core.mockwebserver.ResponseController.GET
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest : BaseTest() {

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

        loginRobot {
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            closeKeyboard()
            clickLoginButton()
            acceptGenericDialog()
        }

        cleanDatabase()
    }

    @Test
    fun shouldGetAuthErrorWhenCredentialsAreWrong() {
        mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_UNAUTHORIZE, 401)

        startLoginActivity()

        loginRobot {
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            closeKeyboard()
            clickLoginButton()
            acceptGenericDialog()
            checkAuthErrorAlertIsVisible()
        }
    }

    @Test
    fun shouldHideLoginButtonIfPasswordIsMissing() {
        startLoginActivity()

        loginRobot {
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
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
        const val API_SYSTEM_INFO = "/api/system/info?.*"

        const val API_ME_RESPONSE_OK = "mocks/user/user.json"
        const val API_ME_UNAUTHORIZE = "mocks/user/unauthorize.json"
        const val API_SYSTEM_INFO_RESPONSE_OK = "mocks/systeminfo/systeminfo.json"

        const val USERNAME = "android"
        const val PASSWORD = "Android123"
    }
}
