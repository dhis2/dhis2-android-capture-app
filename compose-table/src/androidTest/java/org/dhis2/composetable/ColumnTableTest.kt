package org.dhis2.composetable

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

const val MAX_COLUMNS = 48

class ColumnTableTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()
    private val tableColors = TableColors()

    @Test
    fun shouldAllColumnsBuildProperly(){
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        composeTestRule.onRoot().printToLog(COMPOSE_TREE)

        val columnsFirstTable = fakeModel[0].tableHeaderModel.tableMaxColumns()
        val columnsSecondTable = fakeModel[1].tableHeaderModel.tableMaxColumns()


        tableRobot(composeTestRule) {
            assert(columnsFirstTable == MAX_COLUMNS)
            assert(columnsSecondTable == MAX_COLUMNS)
        }
    }

    @Test
    fun shouldHighlightColumnHeaderWhenClickingOnHeader(){
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        val firstTableId = fakeModel[0].id!!

        tableRobot(composeTestRule) {
            clickOnHeaderElement(firstTableId, 2, 3)
            assertColumnHeaderBackgroundColor(firstTableId, 2, 3, tableColors.primary)
        }
    }

    @Test
    fun shouldHighlightChildrenColumnWhenSelectingParent(){
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        val firstTableId = fakeModel[0].id!!

        val sonColumnsHighlight = 12
        val grandsonColumnsHighlight = 12
        val maxColumnGrandSon = 48

        tableRobot(composeTestRule) {
            clickOnHeaderElement(firstTableId, 0, 0)
            for (i in 0 until sonColumnsHighlight) {
                assertColumnHeaderBackgroundColor(firstTableId, 1, i, tableColors.primaryLight)
            }
            for (i in 0 until grandsonColumnsHighlight) {
                assertColumnHeaderBackgroundColor(firstTableId, 2, i, tableColors.primaryLight)
            }
            val firstNonHighlightColumn = grandsonColumnsHighlight + 1
            for (i in firstNonHighlightColumn until maxColumnGrandSon) {
                if (i % 2 == 0){
                    assertColumnHeaderBackgroundColor(firstTableId, 2, i, tableColors.headerBackground1)
                } else {
                    assertColumnHeaderBackgroundColor(firstTableId, 2, i, tableColors.headerBackground2)
                }
            }
        }
    }

    @Test
    fun shouldAssertHeaderColumnColorsEvenOdd(){
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        val firstTableId = fakeModel[0].id!!

        val secondRowHeaderChildren = 12
        val thirdRowHeaderChildren = 48

        tableRobot(composeTestRule) {
            //Assert first column rows
            assertColumnHeaderBackgroundColor(firstTableId, 0, 0, tableColors.headerBackground1)
            assertColumnHeaderBackgroundColor(firstTableId, 0, 1, tableColors.headerBackground2)
            assertColumnHeaderBackgroundColor(firstTableId, 0, 2, tableColors.headerBackground1)

            //Assert second column rows
            for (i in 0 until secondRowHeaderChildren) {
                if (i % 2 == 0){
                    assertColumnHeaderBackgroundColor(firstTableId, 1, i, tableColors.headerBackground1)
                } else {
                    assertColumnHeaderBackgroundColor(firstTableId, 1, i, tableColors.headerBackground2)
                }
            }

            //Assert third column rows
            for (i in 0 until thirdRowHeaderChildren) {
                if (i % 2 == 0){
                    assertColumnHeaderBackgroundColor(firstTableId, 2, i, tableColors.headerBackground1)
                } else {
                    assertColumnHeaderBackgroundColor(firstTableId, 2, i, tableColors.headerBackground2)
                }
            }
        }
    }

    private fun initTable(fakeModel: List<TableModel>) {
        composeTestRule.setContent {
            DataTable(
                tableList = fakeModel,
                tableColors = tableColors,
                onDecorationClick = {

                },
                onClick = {

                }
            )
        }
    }
}