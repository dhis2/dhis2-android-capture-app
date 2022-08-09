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

            assertRowHeaderText(firstTableId, "Text 1", 0)
            assertRowHeaderText(firstTableId, "Text 2", 1)
            assertRowHeaderText(firstTableId, "Text 3", 2)

            assertRowHeaderIsClickable(firstTableId, "Text 1",0)
            assertRowHeaderIsClickable(firstTableId, "Text 2",1)
            assertRowHeaderIsClickable(firstTableId, "Text 3",2)
        }

        // Second Table
        tableRobot(composeTestRule) {
            assert(fakeModel[1].tableRows.size == 5)

            assertRowHeaderText(secondTableId, "Number", 0)
            assertRowHeaderText(secondTableId, "Text", 1)
            assertRowHeaderText(secondTableId, "Long Text", 2)
            assertRowHeaderText(secondTableId, "Integer", 3)
            assertRowHeaderText(secondTableId, "Percentage", 4)

            assertRowHeaderIsClickable(secondTableId, "Number",0)
            assertRowHeaderIsClickable(secondTableId, "Text",1)
            assertRowHeaderIsClickable(secondTableId, "Long Text",2)
            assertRowHeaderIsClickable(secondTableId, "Integer",3)
            assertRowHeaderIsClickable(secondTableId, "Percentage",4)
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