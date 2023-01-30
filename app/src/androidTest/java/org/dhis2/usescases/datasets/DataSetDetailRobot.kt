package org.dhis2.usescases.datasets

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo


fun dataSetDetailRobot(dataSetDetailRobot: DataSetDetailRobot.() -> Unit) {
    DataSetDetailRobot().apply {
        dataSetDetailRobot()
    }
}

class DataSetDetailRobot : BaseRobot() {

    fun clickOnAddDataSet() {
        onView(withId(R.id.addDatasetButton)).perform(click())
    }

    fun checkDataSetInList(period: String, orgUnit: String) {
        onView(withId(R.id.recycler))
            .check(matches(hasItem(allOf(
                hasDescendant(withText(period)),
                hasDescendant(withText(orgUnit))
            ))))
    }

    fun checkDataSetIsCompleteAndModified(period: String) {
        onView(withId(R.id.recycler))
            .check(matches(hasItem(allOf(
                hasDescendant(withText(period)),
                hasDescendant(withTagValue(equalTo(R.drawable.ic_event_status_complete))),
                hasDescendant(withTagValue(equalTo(R.drawable.ic_sync_problem_grey)))
            ))))
    }


}