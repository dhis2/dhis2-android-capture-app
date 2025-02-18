package org.dhis2.usescases.datasets.dataSetTable.period

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.dhis2.common.BaseRobot

fun reportPeriodSelectorRobot(
    composeTestRule: ComposeTestRule,
    robotBody: ReportPeriodSelectorRobot.() -> Unit,
) {
    ReportPeriodSelectorRobot(composeTestRule).apply{
        robotBody()
    }
}

class ReportPeriodSelectorRobot(
    private val composeTestRule: ComposeTestRule,
): BaseRobot() {
    fun selectReportPeriod(period: String) {
        composeTestRule.onNodeWithText(period)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
    }
}
