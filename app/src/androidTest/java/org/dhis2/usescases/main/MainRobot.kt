package org.dhis2.usescases.main

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.program.HOME_ITEM
import org.dhis2.usescases.main.program.HOME_ITEMS
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

    fun checkViewIsNotEmpty(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(HOME_ITEMS).assertIsDisplayed()
    }

    fun checkLogInIsLaunched() {
        Intents.intended(allOf(IntentMatchers.hasComponent(LoginActivity::class.java.name)))
    }

    fun checkHomeIsDisplayed(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(HOME_ITEMS).assertIsDisplayed()
    }

    fun openFilters() {
        onView(withId(R.id.filterActionButton)).perform(click())
    }

    fun openProgramByPosition(composeTestRule: ComposeTestRule, position: Int) {
        composeTestRule.onNodeWithTag(HOME_ITEMS)
            .onChildAt(position)
            .performClick()
    }

    fun filterByPeriodToday() {
        onView(withId(R.id.filter)).perform(click())
        onView(withId(R.id.filterLayout))
        onView(withId(R.id.today)).perform(click())
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkItemsInProgram(
        composeTestRule: ComposeTestRule,
        position: Int,
        program: String,
        items: String
    ) {
        composeTestRule.onNodeWithTag(HOME_ITEMS, useUnmergedTree = true)
            .performScrollToIndex(position)
        composeTestRule.onNodeWithTag(HOME_ITEM.format(position))
            .assert(hasText(program))
            .assert(hasText(items, substring = true))
    }

    companion object {
        const val FRAGMENT_TRANSITION = 1500L
        const val LOGOUT_TRANSITION = 2000L
    }
}
