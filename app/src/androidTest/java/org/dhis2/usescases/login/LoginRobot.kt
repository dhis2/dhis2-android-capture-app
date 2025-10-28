package org.dhis2.usescases.login

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.VerificationModes.times
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.common.BaseRobot
import org.dhis2.mobile.login.accounts.ui.screen.ACCOUNT_ITEM_TAG
import org.dhis2.mobile.login.main.ui.screen.CREDENTIALS_ERROR_INFO_BAR_TAG
import org.dhis2.mobile.login.main.ui.screen.CREDENTIALS_LOGIN_BUTTON_TAG
import org.dhis2.mobile.login.main.ui.screen.CREDENTIALS_MANAGE_ACCOUNTS_BUTTON_TAG
import org.dhis2.mobile.login.main.ui.screen.CREDENTIALS_PASSWORD_INPUT_TAG
import org.dhis2.mobile.login.main.ui.screen.CREDENTIALS_USERNAME_INPUT_TAG
import org.dhis2.mobile.login.main.ui.screen.SERVER_VALIDATION_CONTENT_BUTTON_TAG
import org.dhis2.usescases.main.MainActivity
import org.hamcrest.CoreMatchers.allOf


fun loginRobot(
    composeTestRule: ComposeTestRule,
    loginBody: LoginRobot.() -> Unit
) {
    LoginRobot(composeTestRule).apply {
        loginBody()
    }
}

class LoginRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    @OptIn(ExperimentalTestApi::class)
    fun clickOnValidateServerButton() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(SERVER_VALIDATION_CONTENT_BUTTON_TAG),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag(SERVER_VALIDATION_CONTENT_BUTTON_TAG).performClick()
    }

    fun typeServerToValidate(server: String) {
        composeTestRule.onNodeWithTag("INPUT_QR_CODE_FIELD").performTextReplacement(server)
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkServerInputIsDisplayed() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag("INPUT_QR_CODE_FIELD"),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag("INPUT_QR_CODE_FIELD").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun typeUsername(username: String) {
        // Wait for credentials screen to appear
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(CREDENTIALS_USERNAME_INPUT_TAG),
            TIMEOUT,
        )
        // Click on the username input to focus it
        composeTestRule.onNodeWithTag(CREDENTIALS_USERNAME_INPUT_TAG).performClick()
        // Now find the inner text field and type the username
        composeTestRule.onNodeWithTag("INPUT_USER_FIELD").performTextReplacement(username)
    }

    @OptIn(ExperimentalTestApi::class)
    fun typePassword(password: String) {
        // Wait for password field to appear
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(CREDENTIALS_PASSWORD_INPUT_TAG),
            TIMEOUT,
        )
        // Click on the password input to focus it
        composeTestRule.onNodeWithTag(CREDENTIALS_PASSWORD_INPUT_TAG).performClick()
        // Now find the inner text field and type the password
        composeTestRule.onNodeWithTag("INPUT_PASSWORD_TEXT_FIELD").performTextReplacement(password)
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkLoginButtonIsEnabled() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(CREDENTIALS_LOGIN_BUTTON_TAG),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag(CREDENTIALS_LOGIN_BUTTON_TAG)
            .assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkLoginButtonIsDisabled() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(CREDENTIALS_LOGIN_BUTTON_TAG),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag(CREDENTIALS_LOGIN_BUTTON_TAG)
            .assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickLoginButton() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(CREDENTIALS_LOGIN_BUTTON_TAG),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag(CREDENTIALS_LOGIN_BUTTON_TAG).performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkServerValidationErrorIsDisplayed() {
        // Wait for error to appear in the supporting text of the server input
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag("INPUT_QR_CODE_FIELD"),
            TIMEOUT,
        )
        // The error is displayed as supporting text, we just need to verify the field is in error state
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkCredentialsErrorIsDisplayed() {
        // Wait for the InfoBar with error message to appear
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(CREDENTIALS_ERROR_INFO_BAR_TAG),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag(CREDENTIALS_ERROR_INFO_BAR_TAG).assertIsDisplayed()
    }

    fun checkHomeIsDisplayed(expectedTimes: Int = 1) {
        intended(allOf(hasComponent(MainActivity::class.java.name)), times(expectedTimes))
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnManageAccountsButton() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(CREDENTIALS_MANAGE_ACCOUNTS_BUTTON_TAG),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag(CREDENTIALS_MANAGE_ACCOUNTS_BUTTON_TAG).performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkAccountIsListed(serverUrl: String, username: String) {
        val accountTag = "${ACCOUNT_ITEM_TAG}_${serverUrl}_${username}"
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(accountTag),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag(accountTag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnAccount(serverUrl: String, username: String) {
        val accountTag = "${ACCOUNT_ITEM_TAG}_${serverUrl}_${username}"
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(accountTag),
            TIMEOUT,
        )
        composeTestRule.onNodeWithTag(accountTag).performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkTrackingPermissionDialogIsDisplayed() {
        composeTestRule.waitUntilExactlyOneExists(
            hasText("Do you want to help us improve this app?"),
            TIMEOUT,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun acceptTrackingPermission() {
        checkTrackingPermissionDialogIsDisplayed()
        composeTestRule.waitUntilExactlyOneExists(
            hasText("Yes"),
            TIMEOUT,
        )
        composeTestRule.onNode(hasText("Yes")).performClick()
    }
}
