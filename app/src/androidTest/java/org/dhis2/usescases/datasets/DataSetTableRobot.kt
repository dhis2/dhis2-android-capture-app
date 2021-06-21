package org.dhis2.usescases.datasets

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.junit.Assert.assertTrue


fun dataSetTableRobot(dataSetTableRobot: DataSetTableRobot.() -> Unit) {
    DataSetTableRobot().apply {
        dataSetTableRobot()
    }
}

class DataSetTableRobot : BaseRobot() {
    fun clickOnSaveButton() {
        onView(withId(R.id.saveButton)).perform(click())
    }
    fun clickOnPositiveButton(){
        onView(withId(R.id.positive)).perform(click())
    }

    fun clickOnNegativeButton(){
        onView(withId(R.id.negative)).perform(click())
    }

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnMenuReOpen() {
        onView(withText(R.string.re_open)).perform(click())
    }

    fun typeOnEditTextCell(text: String, column: Int, row: Int) {
        onView(withId(row))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FormViewHolder>( //EditTextCustomHolder
                    column, typeChildViewWithId(text, R.id.input_editText)
                )
            )
    }

    fun clickOnEditTextCell(column: Int, row: Int){
        onView(withId(row))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FormViewHolder>(
                    column, clickChildViewWithId(R.id.inputEditText)
                )
            )
    }

    fun acceptDateSelected(){
        onView(withId(R.id.acceptButton)).perform(click())
    }

    fun checkActivityHasNotFinished(activity: DataSetTableActivity) {
        assertTrue(!activity.isDestroyed)
    }
}