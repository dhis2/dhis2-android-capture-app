package org.dhis2.usescases.searchte

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not


fun searchTeiRobot(searchTeiRobot: SearchTeiRobot.() -> Unit) {
    SearchTeiRobot().apply {
        searchTeiRobot()
    }
}

class SearchTeiRobot : BaseRobot() {

    fun closeSearchForm () {
        waitToDebounce(2500)
        onView(withId(R.id.close_filter)).perform(click())
    }

    fun clickOnTEI(teiName: String, teiLastName: String) {
        onView(withId(R.id.scrollView)).perform(
            scrollTo<SearchTEViewHolder>(allOf(hasDescendant(withText(teiName)), hasDescendant(withText(teiLastName)))),
            actionOnItem<SearchTEViewHolder>(allOf(hasDescendant(withText(teiName)), hasDescendant(withText(teiLastName))), click())
        )
    }

    fun checkTEIsDelete(teiName: String, teiLastName: String) {
        onView(withId(R.id.scrollView))
            .check(matches(not(hasItem(allOf(hasDescendant(withText(teiName)), hasDescendant(
                withText(teiLastName)))))))
    }

    fun searchByPosition(searchWord: String, position:Int) {
        waitToDebounce(1000)
        onView(withId(R.id.form_recycler))
            .perform(
                actionOnItemAtPosition<SearchTEViewHolder>(position, typeChildViewWithId(searchWord, R.id.input_editText))
            )
    }

    fun searchByField(searchWord: String, field: String) {
        waitToDebounce(1000)
        onView(withId(R.id.form_recycler))
            .perform(
                actionOnItem<SearchTEViewHolder>(hasDescendant(withText(field)), typeChildViewWithId(searchWord, R.id.input_editText))
            )
    }

    fun clickOnDateField() {
        onView(withId(R.id.form_recycler))
            .perform(
                actionOnItemAtPosition<SearchTEViewHolder>(2, clickChildViewWithId(R.id.inputEditText))
            )
    }

    fun selectSpecificDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(R.id.widget_datepicker)).perform(PickerActions.setDate(year, monthOfYear, dayOfMonth))
    }

    fun acceptDate() {
        onView(withId(R.id.acceptButton)).perform(click())
    }

    fun clickOnFab() {
        onView(withId(R.id.enrollmentButton)).perform(click())
    }

    fun checkListOfSearchTEI(searchWord: String) {
        onView(withId(R.id.scrollView))
            .check(matches(hasItem(hasDescendant(withText(searchWord)))))
    }

    fun checkFilterCount(filterCount: String) {
        onView(withId(R.id.filterCounterSearch))
            .check(matches(withChild(withText(filterCount))))
    }

    fun clickOnProgramSpinner() {
        onView(withId(R.id.spinner_text)).perform(click())
    }

    fun selectAProgram(program: String) {
        /*onView(withId(R.id.spinner_text))
        onData(
            allOf(
                (instanceOf(String::class.java)),
                `is`(selectionText)
            )
        )
            .perform(click())*/

        /*onData(anything())
            .inAdapterView(withId(R.id.spinner_text))
            .onChildView(allOf(withId(R.id.spinner_text), withText(program)))
            .perform(click())*/

        onData(
            allOf(
                `is`(instanceOf(String::class.java)),
                `is`(program)
            )
        ) .perform(click())
    }
}
