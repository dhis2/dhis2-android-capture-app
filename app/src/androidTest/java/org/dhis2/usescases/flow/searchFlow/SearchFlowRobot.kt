package org.dhis2.usescases.flow.searchFlow

import androidx.compose.ui.test.junit4.ComposeTestRule
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.searchte.robot.filterRobot

fun searchFlowRobot(
    composeTestRule: ComposeTestRule,
    searchFlowRobot: SearchFlowRobot.() -> Unit
) {
    SearchFlowRobot(composeTestRule).apply {
        searchFlowRobot()
    }
}

class SearchFlowRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun filterByOpenEnrollmentStatus(enrollmentStatus: String) {
        filterRobot(composeTestRule) {
            clickOnFilter(R.id.search_filter_general)
            clickOnFilterBy(
                R.id.filterRecyclerLayout,
                enrollmentStatus)
            clickOnFilterActiveOption()
            clickOnSortByField(enrollmentStatus)
        }
    }

    fun checkSearchCounters(
        filterAtPositionCount: String,
        filter: String,
        filterTotalCount: String
    ) {
        filterRobot(composeTestRule) {
            checkFilterCounter(filterTotalCount)
            checkCountAtFilter(filter, filterAtPositionCount)
            clickOnFilter(R.id.search_filter_general)
        }
    }
}
