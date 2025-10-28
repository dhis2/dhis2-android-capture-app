package org.dhis2.usescases.main

import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.VerificationModes.times
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.program.HOME_ITEMS
import org.dhis2.usescases.main.program.hasPrograms
import org.hamcrest.CoreMatchers.allOf

fun homeRobot(
    composeTestRule: ComposeTestRule,
    robotBody: MainRobot.() -> Unit
) {
    MainRobot(composeTestRule).apply {
        robotBody()
    }
}

class MainRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

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
        onView(withText(R.string.wipe_data_ok)).perform(click())
        waitToDebounce(LOGOUT_TRANSITION)
    }

    fun clickOnLogout() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.logout_button))
        waitToDebounce(LOGOUT_TRANSITION)
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
        composeTestRule.onNodeWithTag(HOME_ITEMS).assertIsDisplayed()
    }

    fun checkLoginScreenIsDisplayed(expectedTimes: Int = 1) {
        // Verify that LoginActivity was launched twice: once at the start and once after logout
        intended(allOf(hasComponent(LoginActivity::class.java.name)), times(expectedTimes))
    }

    companion object {
        const val FRAGMENT_TRANSITION = 1500L
        const val LOGOUT_TRANSITION = 2000L
    }
}
