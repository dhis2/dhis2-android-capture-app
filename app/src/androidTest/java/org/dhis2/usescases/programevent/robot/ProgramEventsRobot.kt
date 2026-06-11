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
        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAtLeastOneExists(hasText(eventDate))
        composeTestRule.onNodeWithText(eventDate).performClick()
    }

    fun clickOnAddEvent() {
        composeTestRule.onNodeWithTag("ADD_EVENT_BUTTON").performClick()
    }

    fun clickOnMap() {
        composeTestRule.onNodeWithTag("NAVIGATION_BAR_ITEM_Map").performClick()
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
        composeTestRule.waitUntilAtLeastOneExists(hasText("Event completed", true), TIMEOUT)
        composeTestRule.onNodeWithText(eventDate,true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Event completed",true).assertIsDisplayed()
    }

    fun checkEventWasDeleted(eventDate: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(eventDate).assertDoesNotExist()
    }

    fun checkMapIsDisplayed() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("MAP", true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("MAP_CAROUSEL",true).assertIsDisplayed()
    }

    /**
     * Asserts the event card whose displayed date is [eventDate] shows
     * the "Event completed" status. Date-scoped so it works even when
     * the program list contains other completed events.
     */
    @OptIn(ExperimentalTestApi::class)
    fun checkEventCardIsComplete(eventDate: String) {
        // Wait until the card with our date AND the "Event completed" badge
        // is rendered together — handles the brief lag between the Complete
        // call returning and the list refreshing its bound model.
        // EVENT_ITEM is set in the unmerged tree by the design-system ListCard.
        val cardMatcher =
            hasTestTag("EVENT_ITEM") and
                hasAnyDescendant(hasText(eventDate)) and
                hasAnyDescendant(hasText("Event completed", substring = true))
        composeTestRule.waitUntil(timeoutMillis = TIMEOUT) {
            composeTestRule.onAllNodes(cardMatcher, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNode(cardMatcher, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
