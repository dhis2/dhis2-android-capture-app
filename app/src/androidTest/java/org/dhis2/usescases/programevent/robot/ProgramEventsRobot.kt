package org.dhis2.usescases.programevent.robot

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.action.ViewActions.click
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

    @OptIn(ExperimentalTestApi::class)
    fun assertVisibleEventCount(expected: Int) {
        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule
                .onAllNodesWithTag("LIST_CARD_ADDITIONAL_INFO_COLUMN", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("LIST_CARD_ADDITIONAL_INFO_COLUMN", useUnmergedTree = true)
            .assertCountEquals(expected)
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkDataLessEventCardHasNoReportValues(eventDate: String, deMarkers: List<String>) {
        val card = hasText(eventDate, substring = true)
        composeTestRule.waitUntilAtLeastOneExists(card, TIMEOUT)
        val cardTexts = composeTestRule.onNode(card).fetchSemanticsNode().texts()
        val present =
            deMarkers.filter { marker -> cardTexts.any { it.contains(marker, ignoreCase = true) } }
        if (present.isNotEmpty()) {
            throw AssertionError(
                "The data-less event card for '$eventDate' should show no displayInReports " +
                    "values, but these DEs appeared: $present\nActual card texts: $cardTexts",
            )
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkEventCardReportValues(
        eventDate: String,
        expectedEntries: List<Pair<String, String>>,
        expectedCount: Int,
    ) {
        val card = hasText(eventDate, substring = true)
        composeTestRule.waitUntilAtLeastOneExists(card, TIMEOUT)
        val cardTexts = composeTestRule.onNode(card).fetchSemanticsNode().texts()

        val missing =
            expectedEntries.filter { (keyFragment, value) ->
                cardTexts.none { row ->
                    row.contains(keyFragment, ignoreCase = true) && row.trimEnd().endsWith(value)
                }
            }
        val reportRows =
            cardTexts.filter { row ->
                row.contains(": ") && !row.contains(REGISTERED_IN_LABEL, ignoreCase = true)
            }
        if (missing.isNotEmpty() || reportRows.size != expectedCount) {
            throw AssertionError(
                "Report values on the event card for '$eventDate' did not match.\n" +
                    "Expected but MISSING (keyFragment to value): $missing\n" +
                    "Expected $expectedCount report rows, found ${reportRows.size}: $reportRows\n" +
                    "Actual card texts: $cardTexts",
            )
        }
    }

    fun checkEventCardStatusColor(eventDate: String, statusText: String, expectedColor: Color) {
        val actualColor = statusRowColor(eventDate, statusText)
        if (actualColor != expectedColor) {
            throw AssertionError(
                "'$statusText' on the event card for '$eventDate' is colored $actualColor, " +
                    "expected $expectedColor",
            )
        }
    }

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

    fun clickSyncButton() {
        waitForView(withId(R.id.syncButton)).perform(click())
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

    companion object {
        private const val REGISTERED_IN_LABEL = "Registered in"
    }
}
