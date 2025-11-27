package org.dhis2.common.filters

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun filterRobotCommon(robotBody: FiltersRobot.() -> Unit) {
    FiltersRobot().apply {
        robotBody()
    }
}

class FiltersRobot : BaseRobot() {

    fun selectDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        waitForView(withId(R.id.datePicker)).perform(
            PickerActions.setDate(year, monthOfYear, dayOfMonth)
        )
        waitForView(withId(R.id.acceptBtn)).perform(click())
    }
}