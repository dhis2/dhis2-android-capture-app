package org.dhis2.usescases.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.hamcrest.CoreMatchers.allOf

fun homeRobot(robotBody: MainRobot.() -> Unit){
    MainRobot().apply {
        robotBody()
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
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.logout_button))
    }

    fun clickAbout() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.menu_about))
    }

    fun clickJiraIssue() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.menu_jira))
    }

    fun checkViewIsNotEmpty() {
        onView(withId(R.id.program_recycler))
                .check(matches(allOf(isDisplayed(),isNotEmpty())))
    }

    fun filterByPeriodToday() {
        // click on filter, find PERIOD filter and click, today and click
        onView(withId(R.id.filter)).perform(click())
        onView(withId(R.id.filterLayout))
        onView(withId(R.id.today)).perform(click())
    }

}
