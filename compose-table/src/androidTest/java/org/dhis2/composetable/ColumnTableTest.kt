package org.dhis2.composetable

import androidx.compose.material.lightColors
import androidx.compose.ui.test.junit4.createComposeRule
import org.dhis2.composetable.model.FakeModelType
import org.dhis2.composetable.ui.TableColors
import org.junit.Rule
import org.junit.Test

const val MAX_COLUMNS = 48

class ColumnTableTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    private val tableColors = TableColors()

    @Test
    fun shouldAllColumnsBuildProperly() {
        tableRobot(composeTestRule) {
            val fakeModel = initTable(FakeModelType.MULTIHEADER_TABLE)
            val columnsFirstTable = fakeModel[0].tableHeaderModel.tableMaxColumns()
            val columnsSecondTable = fakeModel[1].tableHeaderModel.tableMaxColumns()

            assert(columnsFirstTable == MAX_COLUMNS)
            assert(columnsSecondTable == MAX_COLUMNS)
        }
    }

    @Test
    fun shouldHighlightColumnHeaderWhenClickingOnHeader() {
        tableRobot(composeTestRule) {
            val fakeModel = initTable(FakeModelType.MULTIHEADER_TABLE)
            val firstTableId = fakeModel[0].id!!

            clickOnHeaderElement(firstTableId, 2, 3)
            assertColumnHeaderBackgroundColor(firstTableId, 2, 3, lightColors().primary)
        }
    }

    @Test
    fun shouldHighlightChildrenColumnWhenSelectingParent() {
        tableRobot(composeTestRule) {
            val fakeModel = initTable(FakeModelType.MULTIHEADER_TABLE)
            val firstTableId = fakeModel[0].id!!
            val sonColumnsHighlight = 3
            val grandsonColumnsHighlight = 12
            val maxColumnGrandSon = MAX_COLUMNS

            clickOnHeaderElement(firstTableId, 0, 0)
            for (i in 0 until sonColumnsHighlight) {
                assertColumnHeaderBackgroundColor(firstTableId, 1, i, tableColors.primaryLight)
            }
            for (i in 0 until grandsonColumnsHighlight) {
                assertColumnHeaderBackgroundColor(firstTableId, 2, i, tableColors.primaryLight)
            }
            val firstNonHighlightColumn = grandsonColumnsHighlight + 1
            for (i in firstNonHighlightColumn until maxColumnGrandSon) {
                if (i % 2 == 0) {
                    assertColumnHeaderBackgroundColor(
                        firstTableId,
                        2,
                        i,
                        tableColors.headerBackground1
                    )
                } else {
                    assertColumnHeaderBackgroundColor(
                        firstTableId,
                        2,
                        i,
                        tableColors.headerBackground2
                    )
                }
            }
        }
    }

    @Test
    fun shouldAssertHeaderColumnColorsEvenOdd() {
        tableRobot(composeTestRule) {
            val fakeModel = initTable(FakeModelType.MULTIHEADER_TABLE)
            val firstTableId = fakeModel[0].id!!
            val secondRowHeaderChildren = 12
            val thirdRowHeaderChildren = 48

            //Assert first column rows
            assertColumnHeaderBackgroundColor(firstTableId, 0, 0, tableColors.headerBackground1)
            assertColumnHeaderBackgroundColor(firstTableId, 0, 1, tableColors.headerBackground2)
            assertColumnHeaderBackgroundColor(firstTableId, 0, 2, tableColors.headerBackground1)

            //Assert second column rows
            for (i in 0 until secondRowHeaderChildren) {
                if (i % 2 == 0) {
                    assertColumnHeaderBackgroundColor(
                        firstTableId,
                        1,
                        i,
                        tableColors.headerBackground1
                    )
                } else {
                    assertColumnHeaderBackgroundColor(
                        firstTableId,
                        1,
                        i,
                        tableColors.headerBackground2
                    )
                }
            }

            //Assert third column rows
            for (i in 0 until thirdRowHeaderChildren) {
                if (i % 2 == 0) {
                    assertColumnHeaderBackgroundColor(
                        firstTableId,
                        2,
                        i,
                        tableColors.headerBackground1
                    )
                } else {
                    assertColumnHeaderBackgroundColor(
                        firstTableId,
                        2,
                        i,
                        tableColors.headerBackground2
                    )
                }
            }
        }
    }
}