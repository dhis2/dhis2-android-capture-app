package org.dhis2.usescases.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.BaseTest
import org.dhis2.utils.WebViewActivity
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not

fun loginRobot(loginBody: LoginRobot.() -> Unit) {
    LoginRobot().apply{
        loginBody()
    }
}


class LoginRobot : BaseRobot() {

    fun typeServer(server: String) {
        onView(withId(R.id.server_url_edit)).perform(TypeTextAction(server))
    }

    fun clearServerField(){
        onView(withId(R.id.server_url_edit)).perform(clearText())
    }

    fun typeUsername(username: String) {
        onView(withId(R.id.user_name_edit)).perform(TypeTextAction(username))
    }

    fun typePassword(password: String) {
        onView(withId(R.id.user_pass_edit)).perform(TypeTextAction(password))
    }

    fun cleanPasswordField() {
        onView(withId(R.id.clearPassButton)).perform(click())
    }

    fun clickLoginButton(){
        onView(withId(R.id.login)).perform(click())
    }

    fun checkLoginButtonIsHidden() {
        onView(withId(R.id.login)).check(matches(not(isDisplayed())))
    }

    fun checkAuthErrorAlertIsVisible(){
        onView(withId(R.id.dialogTitle)).check(matches(withText(containsString(LOGIN_ERROR_TITLE))))
    }

    fun checkUnblockSessionViewIsVisible(){
        onView(withId(R.id.cardview_pin)).check(matches(isDisplayed()))
    }

    fun clickAccountRecovery() {
        onView(withId(R.id.account_recovery)).perform(click())
    }

    fun checkWebviewWithRecoveryAccountIsOpened(){
        Intents.intended(CoreMatchers.allOf(hasExtra(WebViewActivity.WEB_VIEW_URL,
                "${BaseTest.MOCK_SERVER_URL}/dhis-web-commons/security/recovery.action"),
                hasComponent(WebViewActivity::class.java.name)))
    }

    companion object {
        const val LOGIN_ERROR_TITLE = "Login error"
    }
}