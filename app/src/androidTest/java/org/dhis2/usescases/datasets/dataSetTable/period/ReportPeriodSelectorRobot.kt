package org.dhis2.usescases.datasets.dataSetTable.period

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import org.dhis2.common.BaseRobot
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Locale

fun reportPeriodSelectorRobot(
    composeTestRule: ComposeTestRule,
    robotBody: ReportPeriodSelectorRobot.() -> Unit,
) {
    ReportPeriodSelectorRobot(composeTestRule).apply {
        robotBody()
    }
}

class ReportPeriodSelectorRobot(
    private val composeTestRule: ComposeTestRule,
) : BaseRobot() {

    fun checkFuturePeriodAvailable(openFuturePeriods: Int) {
        // scroll to first item
        composeTestRule
            .onNodeWithTag("period_selector")
            .performScrollToNode(hasTestTag("period_item_0"))

        // check it matches the latest period
        val futureDate = LocalDate.now().plusMonths(openFuturePeriods).toDate()
        val futurePeriod = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(futureDate)

        composeTestRule
            .onNodeWithText(futurePeriod)
            .assertIsDisplayed()
    }

    fun selectPeriod(idx: Int) {
        composeTestRule
            .onNodeWithTag("period_selector")
            .performScrollToNode(hasTestTag("period_item_${idx}"))
            .performClick()
    }
}
