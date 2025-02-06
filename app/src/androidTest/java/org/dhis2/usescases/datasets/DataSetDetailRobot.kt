package org.dhis2.usescases.datasets

import android.view.View
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListViewHolder
import org.dhis2.utils.AdapterItemPosition
import org.dhis2.utils.AdapterItemTitle
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertTrue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


internal fun dataSetDetailRobot(
    composeTestRule: ComposeContentTestRule,
    dataSetDetailRobot: DataSetDetailRobot.() -> Unit
) {
    DataSetDetailRobot(composeTestRule).apply {
        dataSetDetailRobot()
    }
}

internal class DataSetDetailRobot(
    private val composeTestRule: ComposeContentTestRule
) : BaseRobot() {

    fun clickOnAddDataSet() {
        waitForView(withId(R.id.addDatasetButton)).perform(click())
    }

    fun checkDataSetInList(period: String, orgUnit: String) {
        onView(withId(R.id.recycler))
            .check(
                matches(
                    hasItem(
                        allOf(
                            hasDescendant(withText(period)),
                            hasDescendant(withText(orgUnit))
                        )
                    )
                )
            )
    }

    fun checkDataSetIsCompleteAndModified(period: String) {
        onView(withId(R.id.recycler))
            .check(
                matches(
                    hasItem(
                        allOf(
                            hasDescendant(withText(period)),
                            hasDescendant(withTagValue(equalTo(R.drawable.ic_event_status_complete))),
                            hasDescendant(withTagValue(equalTo(R.drawable.ic_sync_problem_grey)))
                        )
                    )
                )
            )
    }

    fun checkDatasetListIsSortedChronologically() {
        val itemCount = getListItemCount()
        val dateList = mutableListOf<Date>()

        for (i in 0 until itemCount) {
            onView(withId(R.id.recycler)).perform(scrollToPosition<RecyclerView.ViewHolder>(i))
            val itemTitle = getTitleFromRecyclerViewItem(i)
            val date = SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(itemTitle)
            dateList.add(date)
        }

        assertTrue(
            "Items are not sorted in chronological order",
            dateList == dateList.sortedDescending()
        )
    }

    fun getListItemCount(): Int {
        val itemCount = intArrayOf(0)
        onView(withId(R.id.recycler)).perform(
            actionOnItemAtPosition<DataSetListViewHolder>(0, object : ViewAction {
                override fun getConstraints() = null
                override fun getDescription() = "Get item count"
                override fun perform(uiController: UiController, view: View) {
                    itemCount[0] = (view.parent as RecyclerView).adapter?.itemCount ?: 0
                }
            })
        )
        return itemCount[0]
    }

    private fun getTitleFromRecyclerViewItem(
        position: Int,
    ): String {
        val title = composeTestRule.onNode(
            SemanticsMatcher.expectValue(AdapterItemPosition, position)
        ).fetchSemanticsNode().config[AdapterItemTitle]
        return title
    }
}