package org.dhis2.usescases.login

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.dhis2.commons.Constants.EXTRA_DATA
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.commons.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.qrScanner.ScanActivity
import org.hamcrest.CoreMatchers.allOf
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.mockwebserver.ResponseController.Companion.API_ME_PATH
import org.hisp.dhis.android.core.mockwebserver.ResponseController.Companion.API_SYSTEM_INFO_PATH
import org.hisp.dhis.android.core.mockwebserver.ResponseController.Companion.GET
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class LoginTest : BaseTest() {

    @get:Rule
    val ruleLogin = lazyActivityScenarioRule<LoginActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    override fun setUp() {
        super.setUp()
        setupMockServer()
        D2Manager.removeCredentials()
    }

    @Test
    fun loginFlow() {
        mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_RESPONSE_OK)
        mockWebServerRobot.addResponse(GET, API_SYSTEM_INFO_PATH, API_SYSTEM_INFO_RESPONSE_OK)
        mockWebServerRobot.addResponse(
            GET,
            PATH_WEBAPP_GENERAL_SETTINGS,
            API_METADATA_SETTINGS_RESPONSE_ERROR,
            404
        )
        mockWebServerRobot.addResponse(GET, PATH_WEBAPP_INFO, API_METADATA_SETTINGS_INFO_ERROR, 404)
        startLoginActivity()

        loginRobot(composeTestRule) {

            // Test case - [ANDROAPP-4122](https://dhis2.atlassian.net/browse/ANDROAPP-4122)
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clearUsernameField()
            clearPasswordField()
            checkUsernameFieldIsClear()
            checkPasswordFieldIsClear()

            //Test case - [ANDROAPP-4123](https://dhis2.atlassian.net/browse/ANDROAPP-4123)
            checkLoginButtonIsHidden()

            // Test case - [ANDROAPP-4126](https://dhis2.atlassian.net/browse/ANDROAPP-4126)
            enableIntents()
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            clickAccountRecovery()
            checkWebviewWithRecoveryAccountIsOpened()
            pressBack()

            // Test case - [ANDROAPP-4121](https://dhis2.atlassian.net/browse/ANDROAPP-4121)
            mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_UNAUTHORIZE, HTTP_UNAUTHORIZE)
            selectUsernameField()
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clickLoginButton()
            checkAuthErrorAlertIsVisible()
            clickOKAuthErrorAlert()

            // Test case - [ANDROAPP-4121](https://dhis2.atlassian.net/browse/ANDROAPP-4121)
            mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_RESPONSE_OK)
            clearPasswordField()
            typePassword(PASSWORD)
            clickLoginButton()

            //Test case - [ANDROAPP-5184](https://dhis2.atlassian.net/browse/ANDROAPP-5184)
            checkShareDataDialogIsDisplayed()
            clickOnPrivacyPolicy()
            checkPrivacyViewIsOpened()
            pressBack()
            acceptTrackerDialog()
            clickYesOnAcceptTrackerDialog()
        }
        cleanDatabase()
    }

    @Test
    fun shouldGoToPinScreenWhenPinWasSet() {
        preferencesRobot.saveValue(SESSION_LOCKED, true)
        preferencesRobot.saveValue(PIN, PIN_PASSWORD)

        startLoginActivity()

        loginRobot(composeTestRule) {
            checkUnblockSessionViewIsVisible()
        }
    }

    @Test
    fun shouldGenerateLoginThroughQR() {
        enableIntents()
        mockOnActivityForResult()
        startLoginActivity()

        loginRobot(composeTestRule) {
            clickQRButton()
            checkQRScanIsOpened()
            checkURL(MOCK_SERVER_URL)
        }
    }

    private fun mockOnActivityForResult() {
        val intent = Intent().apply {
            putExtra(EXTRA_DATA, MOCK_SERVER_URL)
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(allOf(IntentMatchers.hasComponent(ScanActivity::class.java.name))).respondWith(
            result
        )
    }

    private fun startLoginActivity() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            LoginActivity::class.java
        )
        ruleLogin.launch(intent)

    }

    private fun cleanDatabase() {
        context.deleteDatabase(DB_GENERATED_BY_LOGIN)
    }

    companion object {
        const val HTTP_UNAUTHORIZE = 401
        const val API_ME_RESPONSE_OK = "mocks/user/user.json"
        const val API_ME_UNAUTHORIZE = "mocks/user/unauthorize.json"
        const val API_SYSTEM_INFO_RESPONSE_OK = "mocks/systeminfo/systeminfo.json"
        const val API_METADATA_SETTINGS_RESPONSE_ERROR =
            "mocks/settingswebapp/generalsettings_404.json"
        const val API_METADATA_SETTINGS_INFO_ERROR = "mocks/settingswebapp/infosettings_404.json"
        const val PATH_WEBAPP_GENERAL_SETTINGS =
            "/api/dataStore/ANDROID_SETTING_APP/general_settings?.*"
        const val PATH_WEBAPP_INFO = "/api/dataStore/ANDROID_SETTINGS_APP/info?.*"
        const val DB_GENERATED_BY_LOGIN = "127-0-0-1-8080_test_unencrypted.db"
        const val PIN_PASSWORD = 1234
        const val USERNAME = "test"
        const val PASSWORD = "Android123"
    }
}
