package org.dhis2.usescases.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty

fun homeRobot(robotBody: MainRobot.() -> Unit){
    MainRobot().run {
        robotBody
    }
}

class MainRobot : BaseRobot(){

    fun clickOnNavigationDrawerMenu() = apply {
        onView(withId(R.id.menu)).perform(click())
    }

    fun clickOnSettings() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.sync_manager))
    }

    fun clickOnPin() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.block_button))
    }

    fun clickOnLogout() = apply {
       // onView(anyOf(withText(R.string.log_out), withId(R.id.logout_button))).perform(click())
    }

    fun clickAbout() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.menu_about))
    }

    fun clickJiraIssue() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.menu_jira))
    }

    fun checkViewIsNotEmpty() {
        // not sure if it needs to both validations (displayed and empty)
        onView(withId(R.id.program_recycler))
                .check(matches(isDisplayed()))
        onView(withId(R.id.program_recycler)).check(matches(isNotEmpty()))
    }
}
