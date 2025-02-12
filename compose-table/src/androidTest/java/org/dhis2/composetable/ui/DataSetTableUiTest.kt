package org.dhis2.composetable.ui

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import org.dhis2.composetable.model.FakeTableModels
import org.dhis2.composetable.tableRobot
import org.junit.Rule
import org.junit.Test

class DataSetTableUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldRenderTableList() {
        composeTestRule.setContent {
            val fakeModel = FakeTableModels(LocalContext.current)
            DataTable(
                tableList = fakeModel.getMultiHeaderTables()
            )
        }
    }

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
