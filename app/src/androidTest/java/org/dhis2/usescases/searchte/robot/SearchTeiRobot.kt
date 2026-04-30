package org.dhis2.usescases.searchte.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.tracker.search.ui.screen.SEARCH_PARAMETERS_LIST_TAG
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

fun searchTeiRobot(
    composeTestRule: ComposeTestRule,
    searchTeiRobot: SearchTeiRobot.() -> Unit
) {
    SearchTeiRobot(composeTestRule).apply {
        searchTeiRobot()
    }
}

class SearchTeiRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnTEI(teiName: String) {
        composeTestRule.onNodeWithText("First name: $teiName", true).performClick()
    }

    fun checkTEIsDelete(teiName: String, teiLastName: String) {
        onView(withId(R.id.scrollView))
            .check(
                matches(
                    not(
                        hasItem(
                            allOf(
                                hasDescendant(withText(teiName)), hasDescendant(
                                    withText(teiLastName)
                                )
                            )
                        )
                    )
                )
            )
    }

    @OptIn(ExperimentalTestApi::class)
    fun openNextSearchParameter(parameterValue: String) {
        closeKeyboard()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(SEARCH_PARAMETERS_LIST_TAG)
            .performScrollToNode(hasText(parameterValue))
        composeTestRule.waitUntilAtLeastOneExists(hasText(parameterValue), TIMEOUT)
        composeTestRule.onNodeWithText(parameterValue).performClick()
    }

    fun typeOnNextSearchTextParameter(parameterValue: String) {
        composeTestRule.apply {
            onAllNodesWithTag("INPUT_TEXT_FIELD").onLast().performTextInput(parameterValue)
        }
    }

    fun typeOnDateParameter(dateValue: String) {
        composeTestRule.apply {
            onNodeWithTag("INPUT_DATE_TIME_TEXT_FIELD").performTextInput(dateValue)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnSearch() {
        closeKeyboard()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("SEARCH_BUTTON").performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnShowMap() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("NAVIGATION_BAR_ITEM_Map"), TIMEOUT)
        composeTestRule.onNodeWithTag("NAVIGATION_BAR_ITEM_Map").performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkCarouselTEICardInfo(firstName: String) {
        // Wait for the carousel to appear
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MAP_CAROUSEL"), TIMEOUT)
        composeTestRule.onNodeWithTag("MAP_CAROUSEL", true)
            .assertIsDisplayed()
        // Wait for at least one MAP_ITEM to be rendered
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MAP_ITEM"), TIMEOUT)
        // Wait extra time for data to populate in the cards
        composeTestRule.waitForIdle()
        // Finally assert that the text is displayed
        composeTestRule.onNode(
            hasAnyAncestor(hasTestTag("LIST_CARD_ADDITIONAL_INFO_COLUMN"))
                    and hasText(firstName, ignoreCase = true, substring = true),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    fun clickOnOpenSearch() {
        onView(withId(R.id.openSearchButton)).perform(click())
    }

    fun clickOnEnroll() {
        onView(withId(R.id.createButton)).perform(click())
    }

    // --- Methods for the TB Program search flow test ---

    @OptIn(ExperimentalTestApi::class)
    fun checkAddNewTEIButtonIsDisplayedAndEnabled() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("ADD_NEW_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("ADD_NEW_BUTTON")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkFirstSearchParamIsBarcodeOrQROrUnique(expectedLabel: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_PARAM_ITEM"), TIMEOUT)
        composeTestRule
            .onAllNodesWithTag("SEARCH_PARAM_ITEM")[0]
            .assert(hasText(expectedLabel))
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchParamCount(expectedCount: Int) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_PARAM_ITEM"), TIMEOUT)
        val count = composeTestRule
            .onAllNodesWithTag("SEARCH_PARAM_ITEM")
            .fetchSemanticsNodes()
            .size
        assertEquals(
            "Expected $expectedCount search parameters, but found $count",
            expectedCount,
            count
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchButtonIsDisabled() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("SEARCH_BUTTON").assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchButtonIsEnabled() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SEARCH_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("SEARCH_BUTTON").assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    fun typeOnSearchParameter(label: String, value: String) {
        // Dismiss the keyboard first so the LazyColumn recomposes and exposes all field labels.
        // Without this, items scrolled behind an open keyboard are dropped from composition and
        // waitUntilAtLeastOneExists(hasText(label)) times out on the next field.
        // We do NOT close the keyboard at the end: keeping focus on the typed field lets
        // checkFocusedFieldShowsOperatorSupportingText() read its supporting text.
        // Any focus jump caused by the IMM dismiss is immediately overridden by clicking the label.
        closeKeyboard()
        composeTestRule.waitForIdle()
        // Scroll the LazyColumn to ensure the target field is in composition before clicking.
        composeTestRule
            .onNodeWithTag(SEARCH_PARAMETERS_LIST_TAG)
            .performScrollToNode(hasText(label))
        composeTestRule.waitUntilAtLeastOneExists(hasText(label), TIMEOUT)
        composeTestRule.onNodeWithText(label).performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("INPUT_TEXT_FIELD"), TIMEOUT)
        composeTestRule.onAllNodesWithTag("INPUT_TEXT_FIELD").onLast().performTextInput(value)
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnClearSearch() {
        closeKeyboard()
        composeTestRule.waitForIdle()
        val count = composeTestRule
            .onAllNodesWithTag("INPUT_TEXT_RESET_BUTTON", useUnmergedTree = true)
            .fetchSemanticsNodes().size
        repeat(count) {
            composeTestRule.waitForIdle()
            composeTestRule
                .onAllNodesWithTag("INPUT_TEXT_RESET_BUTTON", useUnmergedTree = true)[0]
                .performScrollTo()
                .performClick()
            // Use hardware back press to dismiss the keyboard that appears after clicking X.
            // Unlike closeKeyboard() (IMM-based), the back key is consumed by the keyboard
            // itself before reaching Compose, so focus does not jump to the first focusable
            // item (Unique ID) in the LazyColumn.
            pressBack()
            composeTestRule.waitForIdle()
        }
    }


    @OptIn(ExperimentalTestApi::class)
    fun checkFocusedFieldShowsOperatorSupportingText() {
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Exact matches only") or
                    hasText("Must match the start of the value") or
                    hasText("Must match the end of the value"),
            TIMEOUT,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkMinCharactersErrorIsDisplayed(vararg fieldLabels: String) {
        composeTestRule.waitForIdle()

        fieldLabels.forEach { label ->
            // Scroll the LazyColumn to compose the target field before asserting.
            // Off-screen items are dropped from composition on slow CI devices, causing
            // waitUntilAtLeastOneExists to time out even though the error is present.
            composeTestRule
                .onNodeWithTag(SEARCH_PARAMETERS_LIST_TAG)
                .performScrollToNode(hasText(label))
            composeTestRule.waitForIdle()

            val containerMatcher =
                hasAnyDescendant(hasText(label)) and
                        hasAnyDescendant(hasText("Enter at least", substring = true))

            composeTestRule.waitUntilAtLeastOneExists(containerMatcher, TIMEOUT)

            val matchingNodes = composeTestRule
                .onAllNodes(containerMatcher)
                .fetchSemanticsNodes()

            assertTrue(
                "Expected a min-characters error associated with field label \"$label\", but none was found.",
                matchingNodes.isNotEmpty(),
            )
        }
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkSearchResultDisplayed(teiName: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText(teiName, substring = true), TIMEOUT)
        composeTestRule.onNodeWithText(teiName, substring = true).assertIsDisplayed()
    }
}
