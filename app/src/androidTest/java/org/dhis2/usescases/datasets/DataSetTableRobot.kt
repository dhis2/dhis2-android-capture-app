package org.dhis2.usescases.datasets

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
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
    fun clickOnRunValidations(){
        onView(withId(R.id.positive)).perform(click())
    }

    fun clickOnNegativeButton(){
        onView(withId(R.id.negative)).perform(click())
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
                RecyclerViewActions.actionOnItemAtPosition<FormViewHolder>( //EditTextCustomHolder
                    column, clickChildViewWithId(R.id.input_editText)
                )
            )
    }

    fun checkActivityHasNotFinished(activity: DataSetTableActivity) {
        assertTrue(!activity.isDestroyed)
    }
}