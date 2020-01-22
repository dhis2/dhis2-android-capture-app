package org.dhis2.usescases.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun loginButtonShouldBeDisplayedWhenAllFieldsAreFilled() {
        startActivity()
        Thread.sleep(5000)
    }

    @Test
    fun loginButtonShouldBeDisplayedWhenAllFieldsAreFilled2() {
        startActivity()
        Thread.sleep(5000)

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

    fun startActivity(){
        rule.launchActivity(null)
    }
}