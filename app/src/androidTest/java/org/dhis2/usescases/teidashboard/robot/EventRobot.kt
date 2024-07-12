package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
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
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.hasCompletedPercentage
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.form.ui.FormViewHolder
import org.dhis2.ui.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.ui.dialogs.bottomsheet.SECONDARY_BUTTON_TAG
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder

fun eventRobot(
    composeTestRule: ComposeTestRule,
    eventRobot: EventRobot.() -> Unit
) {
    EventRobot(composeTestRule).apply {
        eventRobot()
    }
}

class EventRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun scrollToBottomForm() {
        onView(withId(R.id.recyclerView)).perform(scrollToBottomRecyclerView())
    }

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

    fun clickOnReopen() {
        composeTestRule.onNodeWithTag("REOPEN_BUTTON").performClick()
    }

    fun fillRadioButtonForm(numberFields: Int) {
        var formLength = 0

        while (formLength < numberFields) {
            onView(withId(R.id.recyclerView))
                .perform(
                    actionOnItemAtPosition<DashboardProgramViewHolder>(
                        formLength,
                        clickChildViewWithId(R.id.yes)
                    )
                )
            formLength++
        }
    }

    fun acceptUpdateEventDate() {
        composeTestRule.onNodeWithText("OK", true).performClick()
    }

    fun typeOnRequiredEventForm(text: String, position: Int) {
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItemAtPosition<FormViewHolder>( //EditTextCustomHolder
                    position, typeChildViewWithId(text, R.id.input_editText)
                )
            )
    }

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnDelete() {
        onView(withText(R.string.delete)).perform(click())
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
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(100)))
        val formattedDate = formatStoredDateToUI(eventDate)
        composeTestRule.onNodeWithText(formattedDate).assertIsDisplayed()
        composeTestRule.onNodeWithText(eventOrgUnit).assertIsDisplayed()
    }

    fun openEventDetailsSection() {
        composeTestRule.onNodeWithText("Event details").performClick()
    }

    fun checkEventIsOpen() {
        composeTestRule.onNodeWithTag("REOPEN_BUTTON").assertDoesNotExist()
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
