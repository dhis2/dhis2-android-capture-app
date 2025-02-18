package org.dhis2.usescases.datasets

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun dataSetInitialRobot(dataSetInitialRobot: DataSetInitialRobot.() -> Unit) {
    DataSetInitialRobot().apply {
        dataSetInitialRobot()
    }
}

class DataSetInitialRobot : BaseRobot() {

    fun clickOnInputOrgUnit() {
        onView(withId(R.id.dataSetOrgUnitInputLayout)).perform(click())
    }

    fun clickOnInputPeriod() {
        waitForView(withId(R.id.dataSetPeriodInputLayout), 5000)
        onView(withId(R.id.dataSetPeriodInputLayout)).perform(click())
    }

    fun clickOnActionButton() {
        onView(withId(R.id.actionButton)).perform(click())
    }
}
