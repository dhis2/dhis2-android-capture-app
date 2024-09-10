package org.dhis2.usescases.main

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.program.HOME_ITEMS
import org.dhis2.usescases.main.program.HasPrograms
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

    fun clickAbout() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.menu_about))
        waitToDebounce(FRAGMENT_TRANSITION)
    }

    fun clickDeleteAccount() = apply {
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.delete_account))
    }

    fun checkViewIsNotEmpty(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(HOME_ITEMS).assert(
            SemanticsMatcher.expectValue(HasPrograms, true)
        )
    }

    fun checkLogInIsLaunched() {
        Intents.intended(allOf(IntentMatchers.hasComponent(LoginActivity::class.java.name)))
    }

    fun checkHomeIsDisplayed(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(HOME_ITEMS).assertIsDisplayed()
    }

    companion object {
        const val FRAGMENT_TRANSITION = 1500L
        const val LOGOUT_TRANSITION = 2000L
    }
}
