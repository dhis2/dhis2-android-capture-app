package org.dhis2.usescases.main

import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.main.program.HOME_ITEMS
import org.dhis2.usescases.main.program.hasPrograms

fun homeRobot(robotBody: MainRobot.() -> Unit) {
    MainRobot().apply {
        robotBody()
    }
}

class MainRobot : BaseRobot() {

    fun clickOnNavigationDrawerMenu() = apply {
        waitForView(withId(R.id.menu)).perform(click())
    }

    fun clickOnSettings() = apply {
        waitForView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.sync_manager))
        waitToDebounce(FRAGMENT_TRANSITION)
    }

    fun clickOnPin() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.block_button))
    }

    fun clickAbout() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.menu_about))
        waitToDebounce(FRAGMENT_TRANSITION)
    }

    fun clickDeleteAccount() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.delete_account))
    }

    fun checkViewIsNotEmpty(composeTestRule: ComposeTestRule) {
        composeTestRule.waitUntil() {
            composeTestRule.onNodeWithTag(HOME_ITEMS)
                .fetchSemanticsNode().config.getOrNull(hasPrograms) == true
        }
        composeTestRule.onNodeWithTag(HOME_ITEMS).assert(
            SemanticsMatcher.expectValue(hasPrograms, true)
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkHomeIsDisplayed(composeTestRule: ComposeTestRule) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(HOME_ITEMS), TIMEOUT)
        composeTestRule.onNodeWithTag(HOME_ITEMS).assertIsDisplayed()
    }

    companion object {
        const val FRAGMENT_TRANSITION = 1500L
        const val LOGOUT_TRANSITION = 2000L
    }
}
