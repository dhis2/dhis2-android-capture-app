package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.hasCompletedPercentage
import org.dhis2.commons.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.commons.dialogs.bottomsheet.SECONDARY_BUTTON_TAG
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.hamcrest.CoreMatchers.allOf

fun eventRobot(
    composeTestRule: ComposeTestRule,
    eventRobot: EventRobot.() -> Unit
) {
    EventRobot(composeTestRule).apply {
        eventRobot()
    }
}

class EventRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnFormFabButton() {
        waitForView(withId(R.id.actionButton)).perform(click())
    }

    fun clickOnNotNow() {
        composeTestRule.onNodeWithTag(SECONDARY_BUTTON_TAG).performClick()
    }

    fun clickOnCompleteButton() {
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).performClick()
    }

    fun checkSecondaryButtonNotVisible() {
        composeTestRule.onNodeWithTag(SECONDARY_BUTTON_TAG).assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnReopen() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("REOPEN_BUTTON"))
        composeTestRule.onNodeWithTag("REOPEN_BUTTON", useUnmergedTree = true).performClick()
    }

    fun acceptUpdateEventDate() {
        composeTestRule.onNodeWithText("OK", true).performClick()
    }

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnDelete() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            val deleteLabel = getString(R.string.delete)
            composeTestRule.onNodeWithText(deleteLabel).performClick()
        }
    }

    fun clickOnDeleteDialog() {
        onView(withId(R.id.possitive)).perform(click())
    }

    fun clickOnEventDueDate() {
        composeTestRule.onNode(
            hasTestTag("INPUT_DATE_TIME_ACTION_BUTTON") and hasAnySibling(
                hasText("Due date")
            )
        ).assertIsDisplayed().performClick()

    }

    fun selectSpecificDate(currentDate: String, date: String) {
        composeTestRule.onNodeWithTag("DATE_PICKER").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            label = "text",
            substring = true,
            useUnmergedTree = true,
        ).performClick()
        composeTestRule.onNode(
            hasText(currentDate) and hasAnyAncestor(isDialog())
        ).performTextReplacement(date)
    }

    @OptIn(ExperimentalTestApi::class)
    fun typeOnDateParameter(dateValue: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("INPUT_DATE_TIME_TEXT_FIELD"),2000)
        composeTestRule.apply {
            onNodeWithTag("INPUT_DATE_TIME_TEXT_FIELD").performClick()
            onNodeWithTag("INPUT_DATE_TIME_TEXT_FIELD").performTextReplacement(dateValue)
        }
    }

    fun checkEventDetails(eventDate: String, eventOrgUnit: String) {
        waitForView((withId(R.id.completion)))
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(100)))
        val formattedDate = formatStoredDateToUI(eventDate)
        composeTestRule.onNodeWithText(formattedDate).assertIsDisplayed()
        composeTestRule.onNodeWithText(eventOrgUnit).assertIsDisplayed()
    }

    fun checkEventCaptureActivityIsLaunched() {
        Intents.intended(allOf(IntentMatchers.hasComponent(EventCaptureActivity::class.java.name)))
    }

    fun openEventDetailsSection() {
        composeTestRule.onNodeWithText("Event details").performClick()
    }

    fun checkEventIsOpen() {
        composeTestRule.onNodeWithTag("REOPEN_BUTTON").assertIsNotDisplayed()
    }

    private fun formatStoredDateToUI(dateValue: String): String {
        val components = dateValue.split("/")

        val year = components[2]
        val month = if (components[1].length == 1) {
            "0${components[1]}"
        } else {
            components[1]
        }
        val day = if (components[0].length == 1) {
            "0${components[0]}"
        } else {
            components[0]
        }

        return "$day/$month/$year"
    }
}
