package org.dhis2.usescases.datasets

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.hamcrest.CoreMatchers.not

internal fun dataSetInitialRobot(
    composeTestRule: ComposeContentTestRule,
    dataSetInitialRobot: DataSetInitialRobot.() -> Unit) {
    DataSetInitialRobot(composeTestRule).apply {
        dataSetInitialRobot()
    }
}

internal class DataSetInitialRobot(
    private val composeTestRule: ComposeContentTestRule
) : BaseRobot() {

    fun clickOnInputOrgUnit() {
        onView(withId(R.id.dataSetOrgUnitInputLayout)).perform(click())
    }

    fun clickOnInputPeriod() {
        onView(withId(R.id.dataSetPeriodInputLayout)).perform(click())
    }

    fun clickOnActionButton() {
        onView(withId(R.id.actionButton)).perform(click())
    }

    fun clickOnInputCatCombo() {
        onView(withId(R.id.input_layout)).perform(click())
    }

    fun selectCatCombo(catCombo: String) {
        onView(withText(catCombo)).perform(click())
    }

    fun chooseDate(date: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("DATE_PICKER").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            label = "text",
            substring = true,
            useUnmergedTree = true,
        ).performClick()
        composeTestRule.onNodeWithContentDescription("Date", substring = true).performTextReplacement(date)
        composeTestRule.onNodeWithText("Accept", true).performClick()
    }

    fun checkActionInputIsNotDisplayed() {
        waitForView(withId(R.id.actionButton)).check(matches(not(isDisplayed())))
    }

    fun checkActionInputIsDisplayed() {
        waitForView(withId(R.id.actionButton)).check(matches(isDisplayed()))
    }
}
