package org.dhis2.usescases.login

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.dhis2.common.keystore.KeyStoreRobot
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.homeRobot
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.mockwebserver.ResponseController.Companion.API_ME_PATH
import org.hisp.dhis.android.core.mockwebserver.ResponseController.Companion.API_SYSTEM_INFO_PATH
import org.hisp.dhis.android.core.mockwebserver.ResponseController.Companion.GET
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LoginTest : BaseTest() {

    @get:Rule
    val ruleLogin = lazyActivityScenarioRule<LoginActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    override fun setUp() {
        restoreDataBaseOnBeforeAction = false
        super.setUp()
        setupMockServer()
        clearAnalyticsPermission()
        D2Manager.removeCredentials()
    }


    override fun teardown() {
        restoreDataBaseOnBeforeAction = true
        super.teardown()
        // Restore original credentials
        D2Manager.setCredentials(KeyStoreRobot.KEYSTORE_USERNAME, KeyStoreRobot.PASSWORD)
    }

    @Test
    fun shouldLoginWithoutOauth() {
        enableIntents()
        startLoginActivity()

        loginRobot(composeTestRule) {
            // Step: Enter incorrect server URL and validate - should show error
            typeServerToValidate("https://invalid-server.com")
            clickOnValidateServerButton()
            checkServerValidationErrorIsDisplayed()

            // Step: Enter correct server URL and validate - should proceed to credentials
            mockWebServerRobot.addResponse(GET, API_LOGIN_CONFIG, API_LOGIN_CONFIG_RESPONSE, 200)
            typeServerToValidate(MOCK_SERVER_URL)
            clickOnValidateServerButton()

            // Step: Credentials screen should be displayed
            // Enter incorrect credentials - should show error
            mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_UNAUTHORIZE, HTTP_UNAUTHORIZE)
            checkLoginButtonIsDisabled()
            typeUsername("wronguser")
            typePassword("wrongpassword")
            checkLoginButtonIsEnabled()
            clickLoginButton()
            checkCredentialsErrorIsDisplayed()

            // Step: Enter correct credentials - should proceed to tracker dialog
            mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_RESPONSE_OK)
            mockWebServerRobot.addResponse(GET, API_SYSTEM_INFO_PATH, API_SYSTEM_INFO_RESPONSE_OK)
            mockWebServerRobot.addResponse(
                GET,
                PATH_WEBAPP_GENERAL_SETTINGS,
                API_METADATA_SETTINGS_RESPONSE_OK,
                200
            )
            mockWebServerRobot.addResponse(
                GET,
                PATH_WEBAPP_INFO,
                API_METADATA_SETTINGS_INFO_ERROR,
                404
            )

            typeUsername(USERNAME)
            typePassword(PASSWORD)
            checkLoginButtonIsEnabled()
            clickLoginButton()

            // Step: Handle tracking permission dialog
            acceptTrackingPermission()

            // Step: Check home screen is displayed
            checkHomeIsDisplayed(expectedTimes = 1)
        }

        // Step: Open drawer menu and logout
        homeRobot(composeTestRule) {
            clickOnNavigationDrawerMenu()
            clickOnLogout()
            checkLoginScreenIsDisplayed(expectedTimes = 2)
        }

        // Step: Click on manage accounts and verify the account is listed
        loginRobot(composeTestRule) {
            clickOnManageAccountsButton()
            checkAccountIsListed(MOCK_SERVER_URL, USERNAME)
            clickOnAccount(MOCK_SERVER_URL, USERNAME)

            // Step: Enter password and login again
            mockWebServerRobot.addResponse(GET, API_ME_PATH, API_ME_RESPONSE_OK)
            mockWebServerRobot.addResponse(GET, API_SYSTEM_INFO_PATH, API_SYSTEM_INFO_RESPONSE_OK)
            mockWebServerRobot.addResponse(
                GET,
                PATH_WEBAPP_GENERAL_SETTINGS,
                API_METADATA_SETTINGS_RESPONSE_OK,
                200
            )
            mockWebServerRobot.addResponse(
                GET,
                PATH_WEBAPP_INFO,
                API_METADATA_SETTINGS_INFO_ERROR,
                404
            )

            typePassword(PASSWORD)
            checkLoginButtonIsEnabled()
            clickLoginButton()

            // Step: Verify home screen is displayed again
            checkHomeIsDisplayed(expectedTimes = 2)
        }

        // Step: Delete account
        homeRobot(composeTestRule) {
            clickOnNavigationDrawerMenu()
            clickDeleteAccount()
            checkLoginScreenIsDisplayed(expectedTimes = 3)
        }

        loginRobot(composeTestRule) {
            //Step: Verify server input is displayed after account deletion
            checkServerInputIsDisplayed()
        }

        cleanDatabase()
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

    private fun clearAnalyticsPermission() {
        D2Manager.getD2()
            .dataStoreModule()
            .localDataStore()
            .value(ANALYTICS_PERMISSION_KEY)
            .blockingDeleteIfExist()
    }

    companion object {
        const val HTTP_UNAUTHORIZE = 401
        const val API_LOGIN_CONFIG = "/api/loginConfig"

        const val API_LOGIN_CONFIG_RESPONSE = "mocks/loginconfig/legacy_flow_config.json"
        const val API_ME_RESPONSE_OK = "mocks/user/user.json"
        const val API_ME_UNAUTHORIZE = "mocks/user/unauthorize.json"
        const val API_SYSTEM_INFO_RESPONSE_OK = "mocks/systeminfo/systeminfo.json"
        const val API_METADATA_SETTINGS_RESPONSE_OK =
            "mocks/settingswebapp/generalsettings.json"
        const val API_METADATA_SETTINGS_INFO_ERROR = "mocks/settingswebapp/infosettings_404.json"
        const val PATH_WEBAPP_GENERAL_SETTINGS =
            "/api/dataStore/ANDROID_SETTING_APP/general_settings?.*"
        const val PATH_WEBAPP_INFO = "/api/dataStore/ANDROID_SETTINGS_APP/info?.*"
        const val DB_GENERATED_BY_LOGIN =
            "127-0-0-1-8080_android_unencrypted.db"  // Using existing test DB
        const val USERNAME = "android"  // Existing test database username
        const val PASSWORD = "Android123"  // Existing test database password

        private const val ANALYTICS_PERMISSION_KEY = "analytics_permission"

    }
}
