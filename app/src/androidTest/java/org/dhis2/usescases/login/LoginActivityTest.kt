package org.dhis2.usescases.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    companion object {
        const val TEST_URL = "https://play.dhis2.org/android-current"
        const val TEST_USERNAME = "android"
        const val TEST_PASS = "Android123"
    }

    @get:Rule
    val rule = ActivityTestRule(LoginActivity::class.java, false, false)

    @Test
    fun loginButtonShouldBeDisplayedWhenAllFieldsAreFilled() {
        rule.launchActivity(null)
        onView(withId(R.id.login)).check(matches(not(isDisplayed())))

        onView(withId(R.id.server_url_edit)).perform(replaceText(TEST_URL), pressImeActionButton())
        onView(withId(R.id.user_name_edit)).perform(
            replaceText(TEST_USERNAME),
            pressImeActionButton()
        )
        onView(withId(R.id.user_pass_edit)).perform(
            replaceText(TEST_USERNAME),
            pressImeActionButton()
        )

        onView(withId(R.id.login)).check(matches(isDisplayed()))
    }
}
