package org.dhis2.usescases.flow.syncFlow.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListViewHolder

fun dataSetRobot(dataSetRobot: DataSetRobot.() -> Unit) {
    DataSetRobot().apply {
        dataSetRobot()
    }
}

class DataSetRobot : BaseRobot() {

    fun clickOnDataSetAtPosition(position: Int) {
        onView(withId(R.id.recycler))
            .perform(
                actionOnItemAtPosition<DataSetListViewHolder>(position, click())
            )
    }

    fun clickOnSave() {
        onView(withId(R.id.saveButton)).perform(click())
    }

}