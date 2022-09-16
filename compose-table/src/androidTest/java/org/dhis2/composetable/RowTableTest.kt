package org.dhis2.composetable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.model.FakeTableModels
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.ui.DataTable
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableSelection
import org.junit.Rule
import org.junit.Test

const val COMPOSE_TREE = "compose_tree"


class RowTableTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()
    private val tableColors = TableColors()

    @Test
    fun shouldClickOnFirstRowElementAndHighlightAllElements() {
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        val firstTableId = fakeModel[0].id!!

        tableRobot(composeTestRule) {
            clickOnRowHeader(firstTableId, 0)
            assertRowHeaderBackgroundChangeToPrimary(firstTableId, 0, tableColors)
        }
    }

    @Test
    fun should_show_information_icon() {
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        val firstTableId = fakeModel[0].id!!

        tableRobot(composeTestRule) {
            assertInfoIcon(firstTableId, 0)
        }
    }

    @Test
    fun should_all_rows_build_properly() {
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        val firstTableId = fakeModel[0].id!!
        val secondTableId = fakeModel[1].id!!

        tableRobot(composeTestRule) {
            assert(fakeModel[0].tableRows.size == 3)

            assertRowHeaderText(firstTableId, "Text 1", 0)
            assertRowHeaderText(firstTableId, "Text 2", 1)
            assertRowHeaderText(firstTableId, "Text 3", 2)

            assertRowHeaderIsClickable(firstTableId, "Text 1", 0)
            assertRowHeaderIsClickable(firstTableId, "Text 2", 1)
            assertRowHeaderIsClickable(firstTableId, "Text 3", 2)
        }

        tableRobot(composeTestRule) {
            assert(fakeModel[1].tableRows.size == 5)

            assertRowHeaderText(secondTableId, "Number", 0)
            assertRowHeaderText(secondTableId, "Text", 1)
            assertRowHeaderText(secondTableId, "Long Text", 2)
            assertRowHeaderText(secondTableId, "Integer", 3)
            assertRowHeaderText(secondTableId, "Percentage", 4)

            assertRowHeaderIsClickable(secondTableId, "Number", 0)
            assertRowHeaderIsClickable(secondTableId, "Text", 1)
            assertRowHeaderIsClickable(secondTableId, "Long Text", 2)
            assertRowHeaderIsClickable(secondTableId, "Integer", 3)
            assertRowHeaderIsClickable(secondTableId, "Percentage", 4)
        }
    }

    private fun initTable(fakeModel: List<TableModel>) {
        composeTestRule.setContent {
            var tableSelection by remember {
                mutableStateOf<TableSelection>(TableSelection.Unselected())
            }

            DataTable(
                tableList = fakeModel,
                tableColors = tableColors,
                tableSelection = tableSelection,
                tableInteractions = object : TableInteractions {
                    override fun onSelectionChange(newTableSelection: TableSelection) {
                        tableSelection = newTableSelection
                    }
                }
            )
        }
    }
}