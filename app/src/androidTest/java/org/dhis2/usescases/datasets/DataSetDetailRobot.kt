package org.dhis2.usescases.datasets

import android.view.View
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListViewHolder
import org.dhis2.utils.AdapterItemPosition
import org.dhis2.utils.AdapterItemTitle
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertTrue


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

    fun clickOnSection(tag: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertItemWithTextIsDisplayed(text: String, substring: Boolean) {
        composeTestRule.waitUntilExactlyOneExists(
            hasText(text, true),
            timeoutMillis = 3000
        )
        itemWithTextIsDisplayed(text, substring, composeTestRule)
    }

    fun checkDataSetIsCompletedAndModified(orgUnit: String ) {
        waitForView(withId(R.id.recycler))
            .check(matches(isDisplayed()))
        onView(withId(R.id.recycler))
            .check(
                matches(
                    hasItem(
                        allOf(
                            hasDescendant(withText(orgUnit)),
                            hasDescendant(withTagValue(equalTo(R.drawable.ic_event_status_complete))),
                            hasDescendant(withTagValue(equalTo(R.drawable.ic_sync_problem_grey)))
                        )
                    )
                )
            )
    }

    fun checkDataSetIsNotCompletedAndModified(catCombo: String, orgUnit: String ) {
        onView(withId(R.id.recycler))
            .check(
                matches(
                    hasItem(
                        allOf(
                            hasDescendant(withText(catCombo)),
                            hasDescendant(withText(orgUnit)),
                            not(hasDescendant(withTagValue(equalTo(R.drawable.ic_event_status_complete)))),
                        )
                    )
                )
            )
    }

    fun checkDatasetListIsSortedChronologically() {
        val itemCount = getListItemCount()
        val dateList = mutableListOf<Date>()

        for (i in 0 until itemCount) {
            waitForView(withId(R.id.recycler)).perform(scrollToPosition<RecyclerView.ViewHolder>(i))
            val itemTitle = getTitleFromRecyclerViewItem(i)
            val date = SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(itemTitle)
            if (date != null) {
                dateList.add(date)
            }
        }

        assertTrue(
            "Items are not sorted in chronological order",
            dateList == dateList.sortedDescending()
        )
    }

    fun getListItemCount(): Int {
        val itemCount = intArrayOf(0)
        waitForView(withId(R.id.recycler)).perform(
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

    fun checkDataSetRecyclerItemsAreDisplayed(itemCount: Int) {
        waitForView(withId(R.id.recycler))
            .perform(waitForRecyclerViewItems(minItemCount = itemCount))
    }

    private fun waitForRecyclerViewItems(minItemCount: Int = 1, timeoutMs: Long = 5000): ViewAction {
        return object : ViewAction {
            override fun getConstraints() = isAssignableFrom(RecyclerView::class.java)
            override fun getDescription() = "Wait for RecyclerView to have at least $minItemCount items"
            override fun perform(uiController: UiController, view: View) {
                val recyclerView = view as RecyclerView
                val startTime = System.currentTimeMillis()
                while ((recyclerView.adapter?.itemCount ?: 0) < minItemCount) {
                    if (System.currentTimeMillis() - startTime > timeoutMs) {
                        throw AssertionError("RecyclerView did not reach $minItemCount items in $timeoutMs ms")
                    }
                    uiController.loopMainThreadForAtLeast(50)
                }
            }
        }
    }

    private fun getTitleFromRecyclerViewItem(
        position: Int,
    ): String {
        val title = composeTestRule.onNode(
            SemanticsMatcher.expectValue(AdapterItemPosition, position)
        ).fetchSemanticsNode().config[AdapterItemTitle]
        return title
    }

    fun clickOnDataSetAtPosition(index: Int) {
        onView(withId(R.id.recycler))
            .perform(
                actionOnItemAtPosition<DataSetListViewHolder>(index, click())
            )
    }
}