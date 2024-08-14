package org.dhis2.usescases.searchte.robot

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.allElementsWithHolderTypeHave
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.commons.filters.FilterHolder
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
import org.hamcrest.CoreMatchers.allOf

fun filterRobot(
    composeTestRule: ComposeTestRule,
    filterRobot: FilterRobot.() -> Unit
) {
    FilterRobot(composeTestRule).apply {
        filterRobot()
    }
}

class FilterRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnEnrollmentDateFilter() {
        val tag = "DATE OF ENROLLMENT"
        onView(allOf(withId(R.id.filterLayout), hasDescendant(withText(tag)))).perform(click())
    }

    fun clickOnTodayEnrollmentDate(){
        onView(allOf(withId(R.id.today),
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(click())
    }

    fun clickOnFilter() {
        onView(withId(R.id.search_filter_general)).perform(click())
    }

    fun clickOnFilterBy(filter: String) {
        onView(withId(R.id.filterRecyclerLayout))
            .perform(actionOnItem<FilterHolder>(hasDescendant(withText(filter)), click())
        )
    }

    fun clickOnFilterActiveOption() {
        onView(withId(R.id.stateActive)).perform(click())
    }

    fun clickOnFilterCompletedOption() {
        onView(withId(R.id.stateEnrollmentCompleted)).perform(click())
    }

    fun clickOnFilterOverdueOption() {
        onView(withId(R.id.filterRecyclerLayout))
            .perform(scrollToBottomRecyclerView())
        onView(withId(R.id.stateOverdue)).perform(click())
    }

    fun closeFilterRowAtField(filter: String) {
        onView(withId(R.id.filterRecyclerLayout))
            .perform(actionOnItem<FilterHolder>(hasDescendant(withText(filter)), clickChildViewWithId(R.id.filterArrow)))
    }

    fun clickOnSortByField(fieldFilter: String) {
        onView(withId(R.id.filterRecyclerLayout))
            .perform(actionOnItem<FilterHolder>(hasDescendant(withText(fieldFilter)), clickChildViewWithId(R.id.sortingIcon)))
    }

    fun typeOrgUnitField(orgUnit: String) {
        onView(withId(R.id.orgUnitSearchEditText)).perform(TypeTextAction(orgUnit))
        closeKeyboard()
        onView(withId(R.id.addButton)).perform(click())
    }

    fun clickOnNotSync() {
        onView(withId(R.id.stateNotSynced)).perform(click())
    }

    fun clickOnFromToDate() {
        onView(allOf(withId(R.id.fromTo), isDisplayed()))
            .perform(click())
    }

    fun chooseDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(R.id.datePicker)).perform(PickerActions.setDate(year, monthOfYear, dayOfMonth))
        onView(withId(R.id.acceptBtn)).perform(click())
    }

    fun checkEventsAreOverdue() {
        composeTestRule.onAllNodesWithText("overdue", substring = true, useUnmergedTree = true).assertCountEquals(4)
    }

    fun checkTEIWithOrgUnit(orgUnit: String) {
        onView(withId(R.id.scrollView))
            .check(matches(allElementsWithHolderTypeHave(SearchTEViewHolder::class.java,hasDescendant(withText(orgUnit)))))
    }

    fun checkTEINotSync() {
        composeTestRule.onNodeWithText("Sync", useUnmergedTree = true).assertIsDisplayed()
    }

    fun checkFilterCounter(filterCount: String) {
        waitForView(withId(R.id.filterCounter))
        onView(allOf(withId(R.id.filterCounter), isDisplayed(), withParent(withId(R.id.mainToolbar))))
            .check(matches(withChild(withText(filterCount))))
    }

    fun checkCountAtFilter(filter: String, count: String) {
        onView(withId(R.id.filterRecyclerLayout))
            .check(matches(hasItem(allOf(hasDescendant(withText(filter)), hasDescendant(withText(count))))))
    }

    fun checkTeiAreCompleted() {
        composeTestRule.onAllNodesWithText("Enrollment completed", true).assertCountEquals(4)
    }
}