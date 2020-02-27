package org.dhis2.usescases.login

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.hamcrest.CoreMatchers.not

fun loginRobot(loginBody: LoginRobot.() -> Unit) {
    LoginRobot().run{
        loginBody
    }
}

class LoginRobot() : BaseRobot() {

    fun typeServer(server: String) {
        onView(withId(R.id.server_url_edit)).perform(TypeTextAction(server))
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

    fun checkLoginButtonIsHidden () {
        onView(withId(R.id.login)).check(matches(not(isDisplayed())))
    }

    fun clickAccountRecovery() {
        onView(withId(R.id.account_recovery)).perform(click())
    }
}