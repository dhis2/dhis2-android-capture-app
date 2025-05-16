package org.dhis2.usescases.datasets

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.hamcrest.CoreMatchers.not

internal fun dataSetInitialRobot(dataSetInitialRobot: DataSetInitialRobot.() -> Unit) {
    DataSetInitialRobot().apply {
        dataSetInitialRobot()
    }
}

internal class DataSetInitialRobot : BaseRobot() {

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

    fun checkActionInputIsNotDisplayed() {
        onView(withId(R.id.actionButton)).check(matches(not(isDisplayed())))
    }

    fun checkActionInputIsDisplayed() {
        onView(withId(R.id.actionButton)).check(matches(isDisplayed()))
    }
}
