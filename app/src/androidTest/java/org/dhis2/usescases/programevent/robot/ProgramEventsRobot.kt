package org.dhis2.usescases.programevent.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun programEventsRobot(
    composeTestRule: ComposeContentTestRule,
    programEventsRobot: ProgramEventsRobot.() -> Unit
) {
    ProgramEventsRobot(composeTestRule).apply {
        programEventsRobot()
    }
}

class ProgramEventsRobot(val composeTestRule: ComposeContentTestRule) : BaseRobot() {

    @OptIn(ExperimentalTestApi::class)
    fun clickOnEvent(eventDate: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText(eventDate))
        composeTestRule.onNodeWithText(eventDate).performClick()
    }

    fun clickOnAddEvent() {
        onView(withId(R.id.addEventButton)).perform(click())
    }

    fun clickOnMap() {
        onView(withId(R.id.navigation_map_view)).perform(click())
    }



    @OptIn(ExperimentalTestApi::class)
    fun checkEventWasCreatedAndClosed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("EVENT_ITEM"))
        composeTestRule.onNode(
            hasTestTag("EVENT_ITEM")
                    and
                    hasAnyDescendant(
                        hasText("Event completed", true)
                    )
                    and
                    hasAnyDescendant(
                        hasText("View only", true)
                    ),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkEventIsComplete(eventDate: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText("Event completed", true), 2000)
        composeTestRule.onNodeWithText(eventDate,true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Event completed",true).assertIsDisplayed()
    }

    fun checkEventWasDeleted(eventDate: String) {
        composeTestRule.onNodeWithText(eventDate).assertDoesNotExist()
    }

    fun checkMapIsDisplayed() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("MAP", true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("MAP_CAROUSEL",true).assertIsDisplayed()
    }

}