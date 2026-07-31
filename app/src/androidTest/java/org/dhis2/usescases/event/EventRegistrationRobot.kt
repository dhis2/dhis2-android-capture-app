package org.dhis2.usescases.event

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.commons.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.commons.dialogs.bottomsheet.SECONDARY_BUTTON_TAG

fun eventRegistrationRobot(
    composeTestRule: ComposeTestRule,
    eventRegistrationRobot: EventRegistrationRobot.() -> Unit,
) {
    EventRegistrationRobot(composeTestRule).apply {
        eventRegistrationRobot()
    }
}

class EventRegistrationRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    // ── Flow 1: form-lifecycle helpers (ANDROAPP-7620) ────────────────────────

    fun checkSaveButtonIsDisplayed() {
        waitForView(withId(R.id.actionButton)).check(matches(isDisplayed()))
    }

    /**
     * Asserts the top-right `CircularCompletionView` (`R.id.completion`) is
     * visible on the form. Drives ANDROAPP-1012 — the spec says the
     * completion % must be shown in the corner of the event-capture screen.
     * We don't assert a specific value because the workflow's event starts
     * empty; just that the indicator is rendered.
     */
    fun checkCompletionPercentIsDisplayedInCorner() {
        waitForView(withId(R.id.completion)).check(matches(isDisplayed()))
    }

    /**
     * Asserts the event's org-unit name is rendered on the form (scrolls to
     * it if needed). Inherited from the legacy smoke test as a sanity check
     * that the form bound to the event correctly.
     */
    fun checkOrgUnitIsDisplayed(orgUnit: String) {
        composeTestRule.onNodeWithText(orgUnit).performScrollTo().assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkFormIsReadOnly() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("REOPEN_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("REOPEN_BUTTON", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkFieldLabelIsFormName(formName: String, displayName: String) {
        composeTestRule.waitUntilAtLeastOneExists(
            hasText(formName, substring = true),
            TIMEOUT,
        )
        composeTestRule.onNodeWithText(formName, substring = true)
            .assertIsDisplayed()
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

    /**
     * Clicks the "No" option of the form's first visible Yes/No radio
     * group. Used by Flow 1's workflow to fill the mandatory WHOMCH
     * Smoking DE so the event can be completed.
     *
     * Form renders BOOLEAN DEs via `ProvideYesNoRadioButtonInput` which
     * tags its radio buttons with `"true"` / `"false"` uids — combined
     * with the design system's `RADIO_BUTTON_${uid}` pattern, the "No"
     * option's test tag is `RADIO_BUTTON_false`.
     */
    @OptIn(ExperimentalTestApi::class)
    fun clickNoOnMandatoryField() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("RADIO_BUTTON_false"), TIMEOUT)
        composeTestRule.onAllNodesWithTag("RADIO_BUTTON_false")[0]
            .performScrollTo()
            .performClick()
    }

    // ── Flow 2: create-form checks (ANDROAPP-7728) ────────────────────────────

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

    /**
     * Under `ON_UPDATE_AND_INSERT` an empty mandatory DE blocks the save
     * outright: the sheet offers only "Review" (MAIN_BUTTON_TAG) with no
     * "Not now" escape hatch (SECONDARY_BUTTON_TAG absent).
     */
    @OptIn(ExperimentalTestApi::class)
    fun checkImmediateMandatoryBlock() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MANDATORY"), TIMEOUT)
        composeTestRule.onAllNodesWithTag("MANDATORY").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(SECONDARY_BUTTON_TAG).assertCountEquals(0)
    }

    /** Taps the mandatory sheet's only action ("Review") to return to the form. */
    fun dismissMandatoryBlockSheet() {
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Opens the dropdown whose field title contains [label] and picks its first
     * option. Identified by content rather than structure: several
     * `INPUT_DROPDOWN` nodes may coexist and the title is not a semantics
     * sibling of the field, so match on which node's subtree carries the label.
     */
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

    @OptIn(ExperimentalTestApi::class)
    fun waitForSaveBottomSheet() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(SECONDARY_BUTTON_TAG), TIMEOUT)
    }

    /**
     * Scrolls the form's `LazyColumn` (tagged `FORM_VIEW`) to [target]. Fields
     * below the initial viewport are not composed at all, so a plain
     * `performScrollTo()` cannot reach them — that requires the node to exist.
     */
    @OptIn(ExperimentalTestApi::class)
    private fun scrollFormTo(target: SemanticsMatcher) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("FORM_VIEW"), TIMEOUT)
        composeTestRule.onNodeWithTag("FORM_VIEW", useUnmergedTree = true).performScrollToNode(target)
    }

    companion object {
        private const val FIRST_DROPDOWN_ITEM_TAG = "INPUT_DROPDOWN_MENU_ITEM_0"
    }
}
