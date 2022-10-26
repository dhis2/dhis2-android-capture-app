package org.dhis2.composetable.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.model.FakeTableModels
import org.junit.Rule
import org.junit.Test

class DataSetTableUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()

    @Test
    fun shouldRenderTableList() {
        composeTestRule.setContent {
            val fakeModel = FakeTableModels(composeTestRule.activity.applicationContext)
            DataTable(
                tableList = fakeModel.getMultiHeaderTables(),
                tableColors = TableColors(
                    primary = MaterialTheme.colors.primary,
                    primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                )
            )
        }
    }
}