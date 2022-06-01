package org.dhis2.usescases.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.common.viewactions.scrollToPositionRecyclerview
import org.dhis2.usescases.login.LoginActivity
import org.hamcrest.CoreMatchers.allOf

fun homeRobot(robotBody: MainRobot.() -> Unit) {
    MainRobot().apply {
        robotBody()
    }
}

class MainRobot : BaseRobot() {

    fun clickOnNavigationDrawerMenu() = apply {
        onView(withId(R.id.menu)).perform(click())
    }

    fun clickOnSettings() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.sync_manager))
        waitToDebounce(FRAGMENT_TRANSITION)
    }

    fun clickOnPin() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.block_button))
    }

    fun clickOnLogout() {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.logout_button))
        waitToDebounce(LOGOUT_TRANSITION)
    }

    fun clickAbout() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.menu_about))
        waitToDebounce(FRAGMENT_TRANSITION)
    }

    fun clickJiraIssue() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.menu_jira))
    }

    fun clickDeleteAccount() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.delete_account))
    }

    fun checkViewIsNotEmpty() {
        onView(withId(R.id.program_recycler))
            .check(matches(allOf(isDisplayed(), isNotEmpty())))
    }

    fun checkLogInIsLaunched() {
        Intents.intended(allOf(IntentMatchers.hasComponent(LoginActivity::class.java.name)))
    }

    fun checkHomeIsDisplayed() {
        onView(withId(R.id.program_recycler))
            .check(matches(isDisplayed()))
    }

    fun openFilters(){
        onView(withId(R.id.filterActionButton)).perform(click())
    }

    fun openProgramByName(program: String){
        onView(withId(R.id.program_recycler)).perform(actionOnItem<ProgramModelHolder>(
            hasDescendant(withText(program)), click()))
    }

    fun openProgramByPosition(position: Int){
        onView(withId(R.id.program_recycler)).perform(actionOnItemAtPosition<ProgramModelHolder>(position, click()))
    }

    fun filterByPeriodToday() {
        onView(withId(R.id.filter)).perform(click())
        onView(withId(R.id.filterLayout))
        onView(withId(R.id.today)).perform(click())
    }

    fun checkItemsInProgram(position: Int, program: String, items: String) {
        onView(withId(R.id.program_recycler))
            .perform(scrollToPositionRecyclerview(position))
            .check(matches(allOf(
                hasDescendant(withText(program)),
                hasDescendant(withText(items))
            )))
    }

    companion object {
        const val FRAGMENT_TRANSITION = 1500L
        const val LOGOUT_TRANSITION = 2000L
    }
}
