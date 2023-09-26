package org.dhis2.usescases.login

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.ClickDrawableAction
import org.dhis2.ui.dialogs.bottomsheet.CLICKABLE_TEXT_TAG
import org.dhis2.ui.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.usescases.BaseTest.Companion.MOCK_SERVER_URL
import org.dhis2.usescases.about.PolicyView
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.utils.WebViewActivity
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
fun loginRobot(loginBody: LoginRobot.() -> Unit) {
    LoginRobot().apply {
        loginBody()
    }
}

class LoginRobot : BaseRobot() {

    fun typeServer(server: String) {
        onView(withId(R.id.server_url_edit)).perform(TypeTextAction(server))
        closeKeyboard()
    }

    fun clearServerField() {
        onView(withId(R.id.server_url_edit)).perform(clearText())
    }

    fun typeUsername(username: String) {
        onView(withId(R.id.user_name_edit)).perform(TypeTextAction(username))
        pressImeActionButton()
    }

    fun clearUsernameField() {
        onView(withId(R.id.clearUserNameButton)).perform(click())
    }

    fun typePassword(password: String) {
        onView(withId(R.id.user_pass_edit)).perform(TypeTextAction(password))
        pressImeActionButton()
    }

    fun clearPasswordField() {
        onView(withId(R.id.clearPassButton)).perform(click())
    }

    fun clickLoginButton() {
        onView(withId(R.id.login)).perform(click())
    }

    fun clickQRButton() {
        onView(withId(R.id.server_url_edit)).perform(ClickDrawableAction(ClickDrawableAction.RIGHT))
    }

    fun checkLoginButtonIsHidden() {
        onView(withId(R.id.login)).check(matches(not(isEnabled())))
    }

    fun checkAuthErrorAlertIsVisible() {
        onView(withText(LOGIN_ERROR_TITLE)).check(matches(isDisplayed()))
    }

    fun checkUnblockSessionViewIsVisible() {
        onView(withId(R.id.cardview_pin)).check(matches(isDisplayed()))
    }

    fun checkUsernameFieldIsClear() {
        onView(withId(R.id.user_name_edit)).check(matches(withText("")))
    }

    fun checkPasswordFieldIsClear() {
        onView(withId(R.id.user_pass_edit)).check(matches(withText("")))
    }

    fun checkURL(url: String) {
        onView(withId(R.id.server_url_edit)).check(matches(withText(url)))
    }

    fun clickAccountRecovery() {
        onView(withId(R.id.account_recovery)).perform(click())
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
        onView(withId(android.R.id.content)).check(matches(isDisplayed()))
    }

    fun clickOnPrivacyPolicy(composeTestRule: ComposeContentTestRule) {
        composeTestRule.onNodeWithTag(CLICKABLE_TEXT_TAG).performClick()
    }

    fun acceptTrackerDialog(composeTestRule: ComposeContentTestRule){
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).performClick()
    }

    fun checkPrivacyViewIsOpened() {
        Intents.intended(CoreMatchers.allOf(hasComponent(PolicyView::class.java.name)))
    }

    companion object {
        const val LOGIN_ERROR_TITLE = "Login error"
    }
}
