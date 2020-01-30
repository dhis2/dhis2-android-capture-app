package org.dhis2.usescases.login

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.common.KeyStoreRobot
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.hisp.dhis.android.core.arch.storage.internal.AndroidSecureStore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest : BaseTest() {

    @get:Rule
    val ruleLogin = ActivityTestRule(LoginActivity::class.java, false, false)

    @get:Rule
    val mainRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun loginButtonShouldBeDisplayedWhenAllFieldsAreFilled() {
        startLoginActivity()

        //  onView(withId(R.id.login)).check(matches(not(isDisplayed())))
        //  onView(withId(R.id.server_url_edit)).perform(replaceText(TEST_URL), pressImeActionButton())
        //  onView(withId(R.id.user_name_edit)).perform(
        //      replaceText(TEST_USERNAME),
        //      pressImeActionButton()
        //  )
        //  onView(withId(R.id.user_pass_edit)).perform(
        //     replaceText(TEST_USERNAME),
        //     pressImeActionButton()
        // )

    //    onView(withId(R.id.login)).check(matches(isDisplayed()))
    }

    @Test
    fun loginButtonShouldBeDisplayedWhenAllFieldsAreFilled2() {
        startMainActivity()

        //  onView(withId(R.id.login)).check(matches(not(isDisplayed())))
        //  onView(withId(R.id.server_url_edit)).perform(replaceText(TEST_URL), pressImeActionButton())
        //  onView(withId(R.id.user_name_edit)).perform(
        //      replaceText(TEST_USERNAME),
        //      pressImeActionButton()
        //  )
        //  onView(withId(R.id.user_pass_edit)).perform(
        //     replaceText(TEST_USERNAME),
        //     pressImeActionButton()
        // )

        //    onView(withId(R.id.login)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldLogout() {
        // startMainActivity()
        //   mainRobot.clickOnNavigationDrawerMenu()
        //           .clickOnLogout()
    }

    fun startMainActivity(){
        mainRule.launchActivity(null)
    }

    fun startLoginActivity(){
        ruleLogin.launchActivity(null)
    }
}
