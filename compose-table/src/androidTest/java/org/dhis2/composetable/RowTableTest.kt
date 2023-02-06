package org.dhis2.composetable

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.model.FakeTableModels
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableConfiguration
import org.dhis2.composetable.ui.TableTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RowTableTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()
    private val tableColors = TableColors()

    var primaryColor: Color? = null

    @Before
    fun setUp() {
        primaryColor = null
    }

    @Test
    fun shouldClickOnFirstRowElementAndHighlightAllElements() {
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        val firstTableId = fakeModel[0].id!!

        tableRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnRowHeader(firstTableId, 0)
            assertRowHeaderBackgroundChangeToPrimary(
                firstTableId,
                0,
                primaryColor?.let { tableColors.copy(primary = it) } ?: tableColors
            )
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
            TableTheme(
                tableColors = TableColors().copy(primary = MaterialTheme.colors.primary),
                tableConfiguration = TableConfiguration(headerActionsEnabled = false)
            ) {
                primaryColor = MaterialTheme.colors.primary
                DataSetTableScreen(
                    tableScreenState = TableScreenState(
                        tables = fakeModel,
                        selectNext = false
                    ),
                    onCellClick = { _, _, _ -> null },
                    onEdition = {},
                    onCellValueChange = {},
                    onSaveValue = { _, _ -> }
                )
            }
        }
    }
}