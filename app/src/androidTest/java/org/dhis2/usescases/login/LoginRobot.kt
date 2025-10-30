package org.dhis2.usescases.login

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.commons.dialogs.bottomsheet.CLICKABLE_TEXT_TAG
import org.dhis2.mobile.login.main.ui.screen.SERVER_VALIDATION_CONTENT_BUTTON_TAG
import org.dhis2.usescases.BaseTest.Companion.MOCK_SERVER_URL
import org.dhis2.usescases.about.PolicyView
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.utils.WebViewActivity
import org.hamcrest.CoreMatchers


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

    fun clickQRButton() {
        composeTestRule.onNodeWithTag("INPUT_QR_CODE_BUTTON").performClick()
    }


    fun checkAuthErrorAlertIsVisible() {
        waitForView(withText(LOGIN_ERROR_TITLE)).check(matches(isDisplayed()))
    }

    fun clickOKAuthErrorAlert() {
        onView(withText(OK)).perform(click())
    }

    fun checkUnblockSessionViewIsVisible() {
        onView(withId(R.id.cardview_pin)).check(matches(isDisplayed()))
    }

    fun checkURL(url: String) {
        composeTestRule.onNodeWithTag("INPUT_QR_CODE_FIELD").assert(hasText(url))
    }

    fun checkWebviewWithRecoveryAccountIsOpened() {
        Intents.intended(
            CoreMatchers.allOf(
                hasExtra(
                    WebViewActivity.WEB_VIEW_URL,
                    "${MOCK_SERVER_URL}/dhis-web-commons/security/recovery.action"
                ),
                hasComponent(WebViewActivity::class.java.name)
            )
        )
    }

    fun checkQRScanIsOpened() {
        Intents.intended(CoreMatchers.allOf(hasComponent(ScanActivity::class.java.name)))
    }

    fun checkShareDataDialogIsDisplayed() {
        val title = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.improve_app_msg_title)
        composeTestRule.onNodeWithText(title)
    }

    fun clickOnPrivacyPolicy() {
        composeTestRule.onNodeWithTag(CLICKABLE_TEXT_TAG).performClick()
    }

    fun acceptTrackerDialog() {
        val title = InstrumentationRegistry
            .getInstrumentation()
            .targetContext.getString(R.string.improve_app_msg_title)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun clickYesOnAcceptTrackerDialog() {
        composeTestRule.onNodeWithText(context.getString(R.string.yes))
            .performClick()
    }

    fun checkPrivacyViewIsOpened() {
        Intents.intended(CoreMatchers.allOf(hasComponent(PolicyView::class.java.name)))
    }

    companion object {
        const val LOGIN_ERROR_TITLE = "Login error"
        const val OK = "OK"
    }
}
