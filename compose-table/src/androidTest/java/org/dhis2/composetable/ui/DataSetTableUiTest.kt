package org.dhis2.composetable.ui

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import org.dhis2.composetable.model.FakeTableModels
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
}