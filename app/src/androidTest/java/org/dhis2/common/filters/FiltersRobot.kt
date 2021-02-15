package org.dhis2.common.filters

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.DatePickerMatchers.Companion.matchesDate
import org.dhis2.utils.filters.FilterHolder

fun filterRobot(robotBody: FiltersRobot.() -> Unit) {
    FiltersRobot().apply {
        robotBody()
    }
}

class FiltersRobot : BaseRobot() {
    fun openDateFilter() {
        onView(withId(R.id.filterRecycler)).perform(
            RecyclerViewActions.actionOnItemAtPosition<FilterHolder>(0, click())
        )
    }

    fun clickOnFromToDateOption() {
        onView(withId(R.id.fromTo)).perform(click())
    }

    fun selectDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(R.id.widget_datepicker)).perform(
            PickerActions.setDate(year, monthOfYear, dayOfMonth)
        )
    }

    fun acceptDateSelected(){
        onView(withId(R.id.acceptButton)).perform(click())
    }

    fun checkDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(R.id.widget_datepicker)).check(matches(matchesDate(year, monthOfYear, dayOfMonth)))
    }
}