package org.dhis2.usescases.datasets.dataSetTable.period

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
    fun selectReportPeriod(period: String) {
        composeTestRule.onNodeWithText(period)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
    }

    fun checkFuturePeriodAvailable(openFuturePeriods: Int) {
        val futureDate = LocalDate.now().plusMonths(openFuturePeriods).toDate()
        val futurePeriod = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(futureDate)
        composeTestRule.onNodeWithText(futurePeriod).assertIsDisplayed()
    }
}
