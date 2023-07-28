package org.dhis2.usescases.form

import android.app.Activity
import android.view.MenuItem
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.common.viewactions.scrollToPositionRecyclerview
import org.dhis2.form.ui.FormViewHolder
import org.dhis2.ui.dialogs.bottomsheet.SECONDARY_BUTTON_TAG
import org.dhis2.usescases.form.FormTest.Companion.NO_ACTION
import org.dhis2.usescases.form.FormTest.Companion.NO_ACTION_POSITION
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not


fun formRobot(formRobot: FormRobot.() -> Unit) {
    FormRobot().apply {
        formRobot()
    }
}

class FormRobot : BaseRobot() {

    private fun clickOnASpecificSection(sectionLabel: String) {
        val perform = onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<FormViewHolder>(
                    allOf(
                        hasDescendant(withText(sectionLabel)), hasDescendant(
                            withId(R.id.openIndicator)
                        )
                    ), click()
                )
            )
    }

    private fun clickOnSpinner(position: Int) {
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItemAtPosition<FormViewHolder>(
                    position, clickChildViewWithId(R.id.inputEditText)
                )
            )
    }

    fun typeOnSearchInput(searchWord: String) {
        onView(withId(R.id.txtSearch)).perform(typeText(searchWord))
    }

    private fun selectAction(action: String, position: Int) {
        onData(anything())
            .inRoot(isPlatformPopup())
            .atPosition(position)
            .perform(click())
    }

    fun resetToNoAction(label: String, position: Int) {
        clickOnSpinner(position)
        selectAction(NO_ACTION, NO_ACTION_POSITION)
    }

    fun checkHiddenField(label: String) {
        onView(withId(R.id.recyclerView))
            .check(matches(not(hasItem(withText(label)))))
    }

    fun checkHiddenSection(label: String) {
        clickOnASpecificSection(label)
        onView(withId(R.id.recyclerView))
            .check(matches(not(hasItem(withText(label)))))
    }

    fun checkValueWasAssigned(value: String) {
        onView(withId(R.id.recyclerView))
            .check(
                matches(
                    hasItem(
                        allOf(
                            hasDescendant(withId(R.id.input_editText)),
                            hasDescendant(withText(value))
                        )
                    )
                )
            )
    }

    fun checkWarningIsShown() {
        onView(withId(R.id.recyclerView))
            .check(matches(hasItem(hasDescendant(withText("Warning with Current Event ")))))
    }

    fun checkErrorIsShown() {
        onView(withId(R.id.recyclerView))
            .check(matches(hasItem(hasDescendant(withText("Error with current event ")))))
    }

    fun checkPopUpWithMessageOnCompleteIsShown(message: String, composeTestRule: ComposeTestRule) {
        composeTestRule.onAllNodesWithTag(message).onFirst().assertExists()
    }

    fun checkIndicatorIsDisplayed(name: String, value: String) {
        onView(withId(R.id.indicator_name))
            .check(matches(allOf(isDisplayed(), withText(name))))
        onView(withId(R.id.indicator_value))
            .check(matches(allOf(isDisplayed(), withText(value))))
    }

    fun checkLabel(label: String, position: Int) {
        onView(withId(R.id.recyclerView))
            .check(matches(atPosition(position, hasDescendant(withText(label)))))
    }

    fun clickOnSaveForm() {
        onView(withId(R.id.actionButton)).perform(click())
    }

    fun checkHiddenOption(label: String, position: Int) {
        clickOnSpinner(position)
        onView(allOf(instanceOf(MenuItem::class.java), hasDescendant(withText(label))))
            .check(doesNotExist())
        selectAction("", 0)
    }

    fun checkDisplayedOption(label: String, position: Int, activity: Activity) {
        clickOnSpinner(position)
        onView(withText(label))
            .inRoot(withDecorView(not(`is`(activity.window.decorView))))
            .check(matches(isDisplayed()))
        selectAction("", 0)
    }

    fun clickOnNotNow(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(SECONDARY_BUTTON_TAG).performClick()
    }

    fun clickOnSelectOption(label: String, position: Int, option: String, optionPosition: Int) {
        clickOnSpinner(position)
        selectAction(option, optionPosition)
    }

    fun scrollToBottomForm() {
        onView(withId(R.id.recyclerView)).perform(scrollToBottomRecyclerView())
    }

    fun scrollToPositionForm(position: Int) {
        onView(withId(R.id.recyclerView)).perform(scrollToPositionRecyclerview(position))
    }

    fun goToAnalytics() {
        onView(withId(R.id.navigation_analytics)).perform(click())
    }

    fun goToDataEntry() {
        onView(withId(R.id.navigation_data_entry)).perform(click())
    }
}