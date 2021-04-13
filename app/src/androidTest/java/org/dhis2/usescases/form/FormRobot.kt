package org.dhis2.usescases.form

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.usescases.form.FormTest.Companion.NO_ACTION
import org.dhis2.usescases.form.FormTest.Companion.NO_ACTION_POSITION
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not

fun formRobot(formRobot: FormRobot.() -> Unit) {
    FormRobot().apply {
        formRobot()
    }
}

class FormRobot : BaseRobot() {

    private fun clickOnASpecificSection(sectionLabel: String) {
        onView(withId(R.id.recyclerView))
            .perform(actionOnItem<FormViewHolder>(allOf(hasDescendant(withText(sectionLabel)), hasDescendant(
                withId(R.id.openIndicator))), click()))
    }

    private fun clickOnSpinner(label: String, position: Int) {
        onView(withId(R.id.recyclerView))
            .perform(actionOnItemAtPosition<FormViewHolder>(
                position, clickChildViewWithId(R.id.input_editText))
            )
    }

    fun typeOnSearchInput(searchWord: String) {
        onView(withId(R.id.txtSearch)).perform(typeText(searchWord))
    }

    private fun selectAction(action: String, position: Int) {
        onView(allOf(withId(R.id.title), withText(action))).perform(click())
    }

    fun resetToNoAction(label: String, position: Int) {
        clickOnSpinner(label, position)
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
            .check(matches(hasItem(allOf(hasDescendant(withId(R.id.input_editText)), hasDescendant(withText(value))))))
    }

    fun checkWarningIsShown() {
        onView(withId(R.id.recyclerView))
            .check(matches(hasItem(hasDescendant(withText("Warning with Current Event ")))))
    }

    fun checkErrorIsShown() {
        onView(withId(R.id.recyclerView))
            .check(matches(hasItem(hasDescendant(withText("Error with current event ")))))
    }

    fun checkPopUpWithMessageOnCompleteIsShown(message: String) {
        onView(withId(R.id.txtMessageOnComplete))
            .check(matches(allOf(isDisplayed(), withText(containsString(message)))))
    }

    fun clickOnSaveForm() {
        onView(withId(R.id.actionButton)).perform(click())
    }

    fun clickOnSelectOption(label: String, position: Int, option: String, optionPosition: Int) {
        clickOnSpinner(label, position)
        selectAction(option, optionPosition)
    }

    fun scrollToBottomForm() {
        onView(withId(R.id.recyclerView)).perform(scrollToBottomRecyclerView())
    }
}