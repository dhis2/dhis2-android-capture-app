package org.dhis2.usescases.event

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.commons.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.commons.dialogs.bottomsheet.SECONDARY_BUTTON_TAG
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity

fun eventRegistrationRobot(
    composeTestRule: ComposeTestRule,
    eventRegistrationRobot: EventRegistrationRobot.() -> Unit,
) {
    EventRegistrationRobot(composeTestRule).apply {
        eventRegistrationRobot()
    }
}

class EventRegistrationRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun waitForFormToOpen() {
        waitUntilActivityVisible<EventCaptureActivity>()
    }

    // ── Flow 1: form-lifecycle helpers (ANDROAPP-7620) ────────────────────────

    fun checkSaveButtonIsDisplayed() {
        waitForView(withId(R.id.actionButton)).check(matches(isDisplayed()))
    }

    fun checkCompletionPercentIsDisplayedInCorner() {
        waitForView(withId(R.id.completion)).check(matches(isDisplayed()))
    }

    fun checkOrgUnitIsDisplayed(orgUnit: String) {
        composeTestRule.onNodeWithText(orgUnit).performScrollTo().assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkFormIsReadOnly() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("REOPEN_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("REOPEN_BUTTON", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun checkFieldLabelIsFormName(formName: String, displayName: String) {
        checkFormFieldLabelIsDisplayed(formName)
        // The verbose displayName must NOT be rendered as a field label.
        composeTestRule.onAllNodesWithText(displayName).assertCountEquals(0)
    }

    fun checkNoLegacyUpdateActionIsPresent() {
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Update").assertCountEquals(0)
    }

    fun clickSyncButton() {
        waitForView(withId(R.id.syncButton)).perform(click())
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickNoOnMandatoryField() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("RADIO_BUTTON_false"), TIMEOUT)
        composeTestRule.onAllNodesWithTag("RADIO_BUTTON_false")[0]
            .performScrollTo()
            .performClick()
    }

    // ── Flow 2: create-form checks (ANDROAPP-7728)

    /** Asserts the stage's custom event-date label is shown (ANDROAPP-899). */
    fun checkEventDateLabelIsDisplayed(label: String) {
        checkFormFieldLabelIsDisplayed(label)
    }

    /** Asserts the non-default category-combo field is shown (ANDROAPP-844). */
    fun checkCategoryFieldIsDisplayed(categoryName: String) {
        checkFormFieldLabelIsDisplayed(categoryName)
    }

    @OptIn(ExperimentalTestApi::class)
    private fun checkFormFieldLabelIsDisplayed(label: String) {
        val labelMatcher = hasText(label, substring = true)
        composeTestRule.waitUntilAtLeastOneExists(labelMatcher, TIMEOUT)
        scrollFormTo(labelMatcher)
        composeTestRule.onAllNodesWithText(label, substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkImmediateMandatoryBlock() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MANDATORY"), TIMEOUT)
        composeTestRule.onAllNodesWithTag("MANDATORY").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(SECONDARY_BUTTON_TAG).assertCountEquals(0)
    }

    /** Taps the mandatory sheet's only action ("Review") and waits until the sheet is gone. */
    @OptIn(ExperimentalTestApi::class)
    fun dismissMandatoryBlockSheet() {
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag(MAIN_BUTTON_TAG), TIMEOUT)
    }

    @OptIn(ExperimentalTestApi::class)
    fun selectFirstDropdownOption(label: String) {
        scrollFormTo(hasText(label, substring = true))
        val dropdowns =
            composeTestRule
                .onAllNodesWithTag("INPUT_DROPDOWN", useUnmergedTree = true)
                .fetchSemanticsNodes()
        val index =
            dropdowns.indexOfFirst { node ->
                node.subtreeTexts().any { it.contains(label, ignoreCase = true) }
            }
        if (index < 0) {
            throw AssertionError(
                "No dropdown field titled '$label'. Dropdowns on screen carried: " +
                    dropdowns.map { it.subtreeTexts() },
            )
        }
        composeTestRule
            .onAllNodesWithTag("INPUT_DROPDOWN", useUnmergedTree = true)[index]
            .performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(FIRST_DROPDOWN_ITEM_TAG), TIMEOUT)
        composeTestRule.onNodeWithTag(FIRST_DROPDOWN_ITEM_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    /** Taps the reset (x) button of the dropdown field titled [label], leaving it empty. */
    fun clearDropdown(label: String) {
        scrollFormTo(hasText(label, substring = true))
        composeTestRule.onNode(dropdownResetButton(label), useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
    }

    /** The reset button is only rendered while the dropdown has a selected value. */
    fun checkDropdownHasValue(label: String) {
        scrollFormTo(hasText(label, substring = true))
        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule
                .onAllNodes(dropdownResetButton(label), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * Presses back until the not-saved sheet shows up, then taps "Discard". A previous back can
     * be consumed first: it clears the focused field and, in portrait, shows the navigation bar.
     */
    @OptIn(ExperimentalTestApi::class)
    fun pressBackAndDiscardChanges() {
        val discardButton = hasTestTag(SECONDARY_BUTTON_TAG)
        repeat(MAX_BACK_PRESSES) {
            if (composeTestRule.onAllNodes(discardButton).fetchSemanticsNodes().isEmpty()) {
                pressBack()
                runCatching { composeTestRule.waitUntilAtLeastOneExists(discardButton, BACK_PRESS_TIMEOUT) }
            }
        }
        composeTestRule.onNode(discardButton).performClick()
        composeTestRule.waitForIdle()
    }

    private fun dropdownResetButton(label: String) =
        hasTestTag("INPUT_DROPDOWN_RESET_BUTTON") and
            hasAnyAncestor(hasTestTag("INPUT_DROPDOWN") and hasAnyDescendant(hasText(label, substring = true)))

    @OptIn(ExperimentalTestApi::class)
    fun waitForSaveBottomSheet() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(SECONDARY_BUTTON_TAG), TIMEOUT)
    }

    @OptIn(ExperimentalTestApi::class)
    private fun scrollFormTo(target: SemanticsMatcher) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("FORM_VIEW"), TIMEOUT)
        composeTestRule.onNodeWithTag("FORM_VIEW", useUnmergedTree = true).performScrollToNode(target)
    }

    @OptIn(ExperimentalTestApi::class)
    fun fillNumberFieldWithLabel(label: String, value: String) {
        scrollFormTo(hasText(label, substring = true))
        val numberField = hasTestTag("INPUT_NUMBER_FIELD")
        composeTestRule.waitUntilAtLeastOneExists(numberField, TIMEOUT)
        composeTestRule.onNode(numberField, useUnmergedTree = true)
            .performTextInput(value)
        Espresso.closeSoftKeyboard()
        composeTestRule.onNodeWithTag("FORM_VIEW").performClick()
        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalTestApi::class)
    fun chooseDateForField(label: String, date: String) {
        scrollFormTo(hasText(label, substring = true))
        val actionButton = hasTestTag("INPUT_DATE_TIME_ACTION_BUTTON")
        composeTestRule.waitUntilAtLeastOneExists(actionButton, TIMEOUT)
        composeTestRule.onAllNodesWithTag("INPUT_DATE_TIME_ACTION_BUTTON", useUnmergedTree = true)
            .onLast()
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("DATE_PICKER"), TIMEOUT)
        composeTestRule.onNodeWithTag("DATE_PICKER").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            label = "text",
            substring = true,
            useUnmergedTree = true,
        ).performClick()
        composeTestRule.onNodeWithContentDescription("Date", substring = true).performTextReplacement(date)
        composeTestRule.onNodeWithText(DATE_PICKER_CONFIRM_TEXT, ignoreCase = true).performClick()
        composeTestRule.waitForIdle()
    }

    companion object {
        private const val DATE_PICKER_CONFIRM_TEXT = "OK"
        private const val FIRST_DROPDOWN_ITEM_TAG = "INPUT_DROPDOWN_MENU_ITEM_0"
        private const val MAX_BACK_PRESSES = 3
        private const val BACK_PRESS_TIMEOUT = 3_000L
    }
}
