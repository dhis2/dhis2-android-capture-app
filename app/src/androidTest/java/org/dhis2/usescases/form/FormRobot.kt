package org.dhis2.usescases.form

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.withSize
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextCustomHolder
import org.dhis2.usescases.form.FormTest.Companion.NO_ACTION
import org.dhis2.usescases.form.FormTest.Companion.NO_ACTION_POSITION
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not

fun formRobot(formRobot: FormRobot.() -> Unit) {
    FormRobot().apply {
        formRobot()
    }
}

class FormRobot : BaseRobot() {

    private fun clickOnASpecificSection(sectionLabel: String) {
        onView(withId(R.id.formRecycler))
            .perform(actionOnItem<EditTextCustomHolder>(allOf(hasDescendant(withText(sectionLabel)), hasDescendant(
                withId(R.id.openIndicator))), click()))
    }

    private fun clickOnSpinner(label: String, position: Int) {
        onView(withId(R.id.formRecycler))
            //.perform(actionOnItem<EditTextCustomHolder>(hasDescendant(withText(label)), clickChildViewWithId(R.id.input_editText)))
            .perform(actionOnItemAtPosition<EditTextCustomHolder>(position, clickChildViewWithId(R.id.input_editText)))
        /*onView(withId(R.id.formRecycler))
            .perform(actionOnItem<FormViewHolder>(
                hasDescendant(withText(label)), clickChildViewWithId(R.id.input_editText)
            ))*/

    }

    fun typeOnSearchInput(searchWord: String) {
        onView(withId(R.id.txtSearch)).perform(typeText(searchWord))
    }

    private fun selectAction(action: String, position: Int) {
        //onView(allOf(withId(R.id.spinner_text), withText(action))).perform(click())
        /*onData(anything())
            .inAdapterView(allOf(withId(R.id.spinner_text),
                childAtPosition(
                    withId(android.R.id.list_container)
                )))
            .atPosition(0)
            .perform(click())*/
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(action)))
            .inAdapterView(withId(R.id.input_editText)) //spinner father spinner_text
            .perform(click())
    }

    fun resetToNoAction(label: String, position: Int) {
        clickOnSpinner(label, position)
        selectAction(NO_ACTION, NO_ACTION_POSITION)
    }

    fun checkHiddenField(itemsCount: Int) {
        onView(withId(R.id.formRecycler))
            .check(matches(withSize(itemsCount)))
    }

    fun checkHiddenSection(itemsCount: Int, label: String) {
        clickOnASpecificSection(label)
        onView(withId(R.id.formRecycler)).check(matches(withSize(itemsCount)))
        clickOnASpecificSection(label)
    }

    fun checkValueWasAssigned() {
        onView(withId(R.id.formRecycler))
            .check(matches(hasItem(allOf(hasDescendant(withId(R.id.input_editText)), not(isClickable()), not(isEnabled())))))
    }

    fun checkWarningIsShown() {
        onView(withId(R.id.formRecycler))
            .check(matches(hasItem(withText("Warning with Current Event "))))
    }

    fun checkErrorIsShown() {
        onView(withId(R.id.formRecycler))
            .check(matches(hasItem(withText("Error with current event "))))
    }

    fun clickOnSelectOption(label: String, position: Int, option: String, optionPosition: Int) {
        clickOnSpinner(label, position)
        selectAction(option, optionPosition)
    }
}