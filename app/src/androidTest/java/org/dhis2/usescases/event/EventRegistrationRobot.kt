package org.dhis2.usescases.event

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
     * group. Used by Flow A's workflow to fill the mandatory WHOMCH
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
}
