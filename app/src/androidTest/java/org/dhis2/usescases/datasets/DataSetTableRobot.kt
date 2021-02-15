package org.dhis2.usescases.datasets

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
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
    fun checkActivityHasNotFinished(activity: DataSetTableActivity) {
        assertTrue(!activity.isDestroyed)
    }
}