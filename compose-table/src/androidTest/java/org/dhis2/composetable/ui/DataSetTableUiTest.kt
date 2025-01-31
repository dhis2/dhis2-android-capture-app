package org.dhis2.composetable.ui

import androidx.compose.ui.test.junit4.createComposeRule
import org.dhis2.composetable.tableRobot
import org.junit.Rule
import org.junit.Test

class DataSetTableUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldRenderInfoBarIfTableListIsEmpty() {
        tableRobot(composeTestRule) {
            initEmptyTableAppScreen(
                emptyTablesText = "Section is misconfigured"
            )

            assertInfoBarIsVisible("Section is misconfigured")
        }
    }
}
