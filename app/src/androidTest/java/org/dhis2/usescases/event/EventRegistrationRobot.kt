package org.dhis2.usescases.event

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.hasCompletedPercentage

fun eventRegistrationRobot(
    composeTestRule: ComposeTestRule,
    eventRegistrationRobot: EventRegistrationRobot.() -> Unit,
) {
    EventRegistrationRobot(composeTestRule).apply {
        eventRegistrationRobot()
    }
}

class EventRegistrationRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun checkEventDataEntryIsOpened(completion: Int, orgUnit: String) {
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(completion)))
        composeTestRule.onNodeWithText(orgUnit).performScrollTo().assertIsDisplayed()
    }

    // ── Flow A: form-lifecycle helpers (ANDROAPP-7620) ────────────────────────

    /**
     * Asserts the Save FAB (`R.id.actionButton`) is visible — the event is
     * editable. Drives ANDROAPP-4647.
     */
    fun checkSaveButtonIsDisplayed() {
        waitForView(withId(R.id.actionButton)).check(matches(isDisplayed()))
    }

    /**
     * Asserts the form is in read-only mode — for completed events the app
     * shows the `REOPEN_BUTTON` Compose tag. Drives ANDROAPP-910.
     */
    @OptIn(ExperimentalTestApi::class)
    fun checkFormIsReadOnly() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("REOPEN_BUTTON"), TIMEOUT)
        composeTestRule.onNodeWithTag("REOPEN_BUTTON", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    /**
     * Asserts the data-entry form is rendered with the data element's
     * configured `formName` rather than its `displayName`. Drives ANDROAPP-4011.
     *
     * For Flow A this is invoked with formName = "Diagnosis method"
     * (vs displayName = "XX MAL RDT TRK - Diagnosis Method"). A substring
     * match is used because the field label may include a trailing "*"
     * marker for mandatory fields.
     */
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

    /**
     * Asserts no legacy "Update" action is rendered anywhere in the form
     * (text node, overflow menu, or button). Drives ANDROAPP-5266 — the
     * Update button was replaced by auto-save.
     */
    fun checkNoLegacyUpdateActionIsPresent() {
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Update").assertCountEquals(0)
    }
}
