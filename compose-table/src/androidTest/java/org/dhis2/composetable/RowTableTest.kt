package org.dhis2.composetable

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.model.FakeTableModels
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.ui.DataTable
import org.dhis2.composetable.ui.TableColors
import org.junit.Rule
import org.junit.Test

const val COMPOSE_TREE = "compose_tree"


class RowTableTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()

    @Test
    fun shouldClickOnFirstRowElementAndHighlightAllElements() {
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        composeTestRule.onRoot().printToLog(COMPOSE_TREE)
        val firstTableId = fakeModel[0].id!!

        tableRobot(composeTestRule) {
            clickOnRowHeader(firstTableId, 0)
        }
    }

    @Test
    fun should_all_rows_build_properly() {
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        composeTestRule.onRoot().printToLog(COMPOSE_TREE)

        val firstTableId = fakeModel[0].id!!
        val secondTableId = fakeModel[1].id!!

        // First Table
        tableRobot(composeTestRule) {
            assert(fakeModel[0].tableRows.size == 3)

            assertRowHeaderText(firstTableId, "Tables 2 - DE1", 0)
            assertRowHeaderText(firstTableId, "Tables 2 - DE2", 1)
            assertRowHeaderText(firstTableId, "Tables 2 - DE3", 2)

            assertRowHeaderIsClickable(firstTableId, "Tables 2- DE1",0)
            assertRowHeaderIsClickable(firstTableId, "Tables 2- DE2",1)
            assertRowHeaderIsClickable(firstTableId, "Tables 2- DE3",2)
        }

        // Second Table
        tableRobot(composeTestRule) {
            assert(fakeModel[1].tableRows.size == 5)

            assertRowHeaderText(secondTableId, "Tables 3 - DE1", 0)
            assertRowHeaderText(secondTableId, "Tables 3 - DE2", 1)
            assertRowHeaderText(secondTableId, "Tables 3 - DE3", 2)
            assertRowHeaderText(secondTableId, "Tables 3 - DE4", 3)
            assertRowHeaderText(secondTableId, "Tables 3 - DE5", 4)

            assertRowHeaderIsClickable(secondTableId, "Tables 3- DE1",0)
            assertRowHeaderIsClickable(secondTableId, "Tables 3- DE2",1)
            assertRowHeaderIsClickable(secondTableId, "Tables 3- DE3",2)
            assertRowHeaderIsClickable(secondTableId, "Tables 3- DE4",3)
            assertRowHeaderIsClickable(secondTableId, "Tables 3- DE5",4)
        }
    }

    private fun initTable(fakeModel: List<TableModel>) {
        composeTestRule.setContent {
            DataTable(
                tableList = fakeModel,
                tableColors = TableColors(
                    primary = MaterialTheme.colors.primary,
                    primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                ),
                onDecorationClick = {

                },
                onClick = {

                }
            )
        }
    }
}