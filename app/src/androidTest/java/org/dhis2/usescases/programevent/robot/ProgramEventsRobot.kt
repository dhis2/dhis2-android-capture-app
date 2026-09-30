package org.dhis2.usescases.programevent.robot

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
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

    /**
     * Asserts the list holds exactly [expected] event cards (ANDROAPP-917).
     *
     * Reads the list's `CollectionInfo.rowCount`, which Compose sets to the LazyColumn's
     * TOTAL item count (`LazyLayoutSemanticState`), not the composed count — so this is
     * viewport-independent. Counting rendered `LIST_CARD_ADDITIONAL_INFO_COLUMN` nodes is
     * NOT equivalent: only visible cards compose, so in landscape 4 of 5 were found.
     * The list is one card per event (`items(count = events.itemCount)`, no headers).
     */
    @OptIn(ExperimentalTestApi::class)
    fun assertEventCardCount(expected: Int) {
        // Wait on the UNMERGED tree: the card tag exists only there, so
        // waitUntilAtLeastOneExists (merged) never finds it.
        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule
                .onAllNodesWithTag("LIST_CARD_ADDITIONAL_INFO_COLUMN", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        val lists =
            composeTestRule.onAllNodes(hasScrollAction(), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .mapNotNull { it.config.getOrNull(SemanticsProperties.CollectionInfo) }
                .filter { it.columnCount == 1 && it.rowCount >= 0 }
        val actual =
            lists.singleOrNull()?.rowCount
                ?: throw AssertionError(
                    "Could not identify the event list: found ${lists.size} vertical " +
                        "collections with row counts ${lists.map { it.rowCount }}",
                )
        if (actual != expected) {
            throw AssertionError("Expected $expected event cards in the list but found $actual")
        }
    }

    /**
     * Asserts the card for a data-less event shows none of [deMarkers]: a
     * displayInReports DE appears on a card only when the event has a value
     * for it (ANDROAPP-904).
     */
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

    /**
     * Asserts the card dated [eventDate] shows exactly [expectedCount] report rows,
     * including every [expectedEntries] key-fragment/value pair (ANDROAPP-904).
     *
     * Card rows read `"<key>:  <value>"`; status/sync rows carry no `": "`, so
     * counting `": "` rows minus the org-unit row gives the report-value count.
     * Read from the MERGED card node: the card root sets `mergeDescendants`, so
     * walking children yields nothing and would pass vacuously.
     */
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
