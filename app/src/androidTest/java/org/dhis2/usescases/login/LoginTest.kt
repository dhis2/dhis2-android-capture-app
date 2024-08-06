package org.dhis2.usescases.login

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import org.dhis2.R
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
    fun loginFlow(){
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

            //shouldClearFieldsAndHideLoginButtonWhenClickCredentialXButton()
            // Manual Test case - 4122
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clearUsernameField()
            clearPasswordField()
            checkUsernameFieldIsClear()
            checkPasswordFieldIsClear()

            //Manual Test case - 4123
            checkLoginButtonIsHidden()

            // shouldLaunchWebViewWhenClickAccountRecoveryAndServerIsFilled()
            // Manual Test case 4126
            enableIntents()
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            clickAccountRecovery()
            checkWebviewWithRecoveryAccountIsOpened()
            pressBack()

            // shouldGetAuthErrorWhenCredentialsAreWrong()
            // Manual test case 4121
            mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_UNAUTHORIZE, HTTP_UNAUTHORIZE)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clickLoginButton()
            checkAuthErrorAlertIsVisible()
            clickOKAuthErrorAlert()

            // shouldLoginSuccessfullyWhenCredentialsAreRight()
            mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_RESPONSE_OK)
            clearPasswordField()
            typePassword(PASSWORD)
            clickLoginButton()

            //Manual test case 5184
            acceptTrackerDialog()
            clickYesOnAcceptTrackerDialog()
            composeTestRule.waitForIdle()
            viewHome()
        }
        cleanDatabase()
    }

    @Test
    fun shouldLoginSuccessfullyWhenCredentialsAreRight() {
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
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clickLoginButton()
            acceptTrackerDialog()
            clickYesOnAcceptTrackerDialog()
            composeTestRule.waitForIdle()
            viewHome()
        }
        cleanDatabase()
    }

    @Test
    fun shouldGetAuthErrorWhenCredentialsAreWrong() {
        mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_UNAUTHORIZE, HTTP_UNAUTHORIZE)
        startLoginActivity()
        loginRobot(composeTestRule) {
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clickLoginButton()
            checkAuthErrorAlertIsVisible()
        }
    }

    @Test
    fun shouldHideLoginButtonIfPasswordIsMissing() {
        startLoginActivity()

        loginRobot(composeTestRule) {
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clearPasswordField()
            checkLoginButtonIsHidden()
        }
    }

    @Test
    fun shouldLaunchWebViewWhenClickAccountRecoveryAndServerIsFilled() {
        enableIntents()
        startLoginActivity()
        loginRobot(composeTestRule) {
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            clickAccountRecovery()
            checkWebviewWithRecoveryAccountIsOpened()
        }
    }

    @Test
    fun shouldClearFieldsAndHideLoginButtonWhenClickCredentialXButton() {
        startLoginActivity()
        loginRobot(composeTestRule) {
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clearUsernameField()
            clearPasswordField()
            checkUsernameFieldIsClear()
            checkPasswordFieldIsClear()
            checkLoginButtonIsHidden()
        }
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
    fun shouldGoToHomeScreenWhenUserIsLoggedIn() {
        setupCredentials()
        startLoginActivity()
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

    @Test
    fun shouldDisplayShareDataDialogAndOpenPrivacyPolicy() {
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
            clearServerField()
            typeServer(MOCK_SERVER_URL)
            typeUsername(USERNAME)
            typePassword(PASSWORD)
            clickLoginButton()
            CoroutineScope(Dispatchers.IO).launch {
                delay(2000)
                withContext(Dispatchers.Main) {
                    checkShareDataDialogIsDisplayed()
                    clickOnPrivacyPolicy()
                    checkPrivacyViewIsOpened()
                }
            }
        }
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
        const val API_METADATA_SETTINGS_PROGRAM_RESPONSE_ERROR =
            "mocks/settingswebapp/programsettings_404.json"
        const val API_METADATA_SETTINGS_DATASET_RESPONSE_ERROR =
            "mocks/settingswebapp/datasetsettings_404.json"
        const val API_METADATA_SETTINGS_INFO_ERROR = "mocks/settingswebapp/infosettings_404.json"
        const val PATH_WEBAPP_GENERAL_SETTINGS =
            "/api/dataStore/ANDROID_SETTING_APP/general_settings?.*"
        const val PATH_WEBAPP_INFO = "/api/dataStore/ANDROID_SETTINGS_APP/info?.*"
        const val PATH_APPS = "/api/apps?.*"
        const val DB_GENERATED_BY_LOGIN = "127-0-0-1-8080_test_unencrypted.db"
        const val PIN_PASSWORD = 1234
        const val USERNAME = "test"
        const val PASSWORD = "Android123"
    }
}
