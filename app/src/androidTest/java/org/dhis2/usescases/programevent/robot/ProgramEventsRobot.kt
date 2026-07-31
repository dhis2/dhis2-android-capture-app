package org.dhis2.usescases.programevent.robot

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
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

    @OptIn(ExperimentalTestApi::class)
    fun waitForEventDisplayed(eventDate: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText(eventDate, substring = true), TIMEOUT)
    }

    /**
     * Asserts the status row [statusText] on the card dated [eventDate] is rendered
     * in [expectedColor] (ANDROAPP-910).
     */
    fun checkEventCardStatusColor(eventDate: String, statusText: String, expectedColor: Color) {
        val actualColor = statusRowColor(eventDate, statusText)
        if (actualColor != expectedColor) {
            throw AssertionError(
                "'$statusText' on the event card for '$eventDate' is colored $actualColor, " +
                    "expected $expectedColor",
            )
        }
    }

    /**
     * The colour of the span covering [statusText] on the card.
     *
     * The colour lives in a `SpanStyle` embedded in the row's own `AnnotatedString`
     * (`getKeyValueAnnotatedString` wraps the value in `withStyle`), not in any
     * semantics colour property. Pick the span whose RANGE covers the value's
     * position: even a keyless status row gets a leading grey KEY span first
     * (`key` defaults to `""`), so "first span with a colour" grabs the wrong one.
     */
    @OptIn(ExperimentalTestApi::class)
    private fun statusRowColor(eventDate: String, statusText: String): Color? {
        val card = hasText(eventDate, substring = true)
        composeTestRule.waitUntilAtLeastOneExists(card, TIMEOUT)
        val annotatedTexts =
            composeTestRule.onNode(card).fetchSemanticsNode()
                .config.getOrNull(SemanticsProperties.Text) ?: emptyList()
        val statusRow =
            annotatedTexts.firstOrNull { it.text.contains(statusText, ignoreCase = true) }
                ?: throw AssertionError(
                    "Event card for '$eventDate' does not show '$statusText'. Actual card texts: " +
                        annotatedTexts.map { it.text },
                )
        val valueStart = statusRow.text.indexOf(statusText, ignoreCase = true)
        return statusRow.spanStyles
            .firstOrNull { span ->
                span.item.color != Color.Unspecified &&
                    valueStart >= span.start &&
                    valueStart < span.end
            }?.item?.color
    }

    fun clickOnMap() {
        composeTestRule.onNodeWithTag("NAVIGATION_BAR_ITEM_Map").performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkEventIsComplete(eventDate: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText("Event completed", true), TIMEOUT)
        composeTestRule.onNodeWithText(eventDate, true).assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Event completed", true)[0].assertIsDisplayed()
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
}
