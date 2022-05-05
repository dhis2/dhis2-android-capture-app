package org.dhis2.usescases.flow.searchFlow

import org.dhis2.common.BaseRobot
import org.dhis2.usescases.searchte.robot.filterRobot
import org.dhis2.usescases.searchte.robot.searchTeiRobot

fun searchFlowRobot(searchFlowRobot: SearchFlowRobot.() -> Unit) {
    SearchFlowRobot().apply {
        searchFlowRobot()
    }
}

class SearchFlowRobot : BaseRobot() {

    fun filterByOpenEnrollmentStatus(enrollmentStatus: String) {
        filterRobot {
            clickOnFilter()
            clickOnFilterBy(enrollmentStatus)
            clickOnFilterActiveOption()
            clickOnSortByField(enrollmentStatus)
        }
    }

    fun checkSearchCounters(searchCount: String, filterAtPositionCount: String, filter: String, filterTotalCount: String) {
        filterRobot {
            checkFilterCounter(filterTotalCount)
            checkCountAtFilter(filter, filterAtPositionCount)
            closeSearchForm()
        }
    }

    fun checkTEIEnrollment() {
        filterRobot {
            checkTEIsAreOpen()
            checkTEINotSync()
        }
    }

}