package org.dhis2.composetable

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.data.TableAppScreenOptions
import org.dhis2.composetable.model.FakeModelType
import org.dhis2.composetable.model.FakeTableModels
import org.dhis2.composetable.model.KeyboardInputType
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.ui.CELL_ERROR_UNDERLINE_TEST_TAG
import org.dhis2.composetable.ui.CELL_TEST_TAG
import org.dhis2.composetable.ui.CELL_VALUE_TEST_TAG
import org.dhis2.composetable.ui.CellSelected
import org.dhis2.composetable.ui.ColumnBackground
import org.dhis2.composetable.ui.ColumnIndexHeader
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.DataTable
import org.dhis2.composetable.ui.DrawableId
import org.dhis2.composetable.ui.HEADER_CELL
import org.dhis2.composetable.ui.HasError
import org.dhis2.composetable.ui.INFO_ICON
import org.dhis2.composetable.ui.INPUT_ERROR_MESSAGE_TEST_TAG
import org.dhis2.composetable.ui.INPUT_ICON_TEST_TAG
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.composetable.ui.INPUT_TEST_TAG
import org.dhis2.composetable.ui.InfoIconId
import org.dhis2.composetable.ui.IsBlocked
import org.dhis2.composetable.ui.LocalTableSelection
import org.dhis2.composetable.ui.MANDATORY_ICON_TEST_TAG
import org.dhis2.composetable.ui.MainLabel
import org.dhis2.composetable.ui.RowBackground
import org.dhis2.composetable.ui.RowIndex
import org.dhis2.composetable.ui.RowIndexHeader
import org.dhis2.composetable.ui.SecondaryLabels
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableId
import org.dhis2.composetable.ui.TableIdColumnHeader
import org.dhis2.composetable.ui.TableSelection
import org.dhis2.composetable.utils.KeyboardHelper
import org.junit.Assert

fun tableRobot(
    composeTestRule: ComposeContentTestRule,
    tableRobot: TableRobot.() -> Unit
) {
    TableRobot(composeTestRule).apply {
        tableRobot()
    }
}

class TableRobot(
    private val composeTestRule: ComposeContentTestRule
) {

    lateinit var onSaveTableCell: TableCell
    val keyboardHelper = KeyboardHelper(composeTestRule, timeout = 3000L)

    fun initTable(
        context: Context,
        fakeModelType: FakeModelType,
        tableColors: TableColors = TableColors(),
        onSave: (TableCell) -> Unit = {}
    ): List<TableModel> {
        val fakeModel = FakeTableModels(context).getMultiHeaderTables(fakeModelType)
        composeTestRule.setContent {
            var tableSelection by remember {
                mutableStateOf<TableSelection>(TableSelection.Unselected())
            }

            CompositionLocalProvider(
                LocalTableSelection provides tableSelection
            ) {
                DataTable(
                    tableList = fakeModel,
                    tableInteractions = object : TableInteractions {
                        override fun onSelectionChange(newTableSelection: TableSelection) {
                            tableSelection = newTableSelection
                        }
                    }
                )
            }
        }
        return fakeModel
    }

    fun initTableAppScreen(
        context: Context,
        fakeModelType: FakeModelType,
        tableAppScreenOptions: TableAppScreenOptions = TableAppScreenOptions(),
        onSave: (TableCell) -> Unit = {}
    ): List<TableModel> {
        val fakeModel = FakeTableModels(context).getMultiHeaderTables(fakeModelType)
        val screenState = TableScreenState(fakeModel, false)
        composeTestRule.setContent {
            keyboardHelper.view = LocalView.current
            var model by remember { mutableStateOf(screenState) }
            DataSetTableScreen(
                tableScreenState = model,
                onCellClick = { tableId, cell, _ ->
                    if (tableAppScreenOptions.requiresTextInput(tableId, cell.row!!)) {
                        TextInputModel(
                            id = cell.id ?: "",
                            mainLabel = fakeModel.find { it.id == tableId }?.tableRows?.find {
                                cell.id?.contains(it.rowHeader.id!!) == true
                            }?.rowHeader?.title ?: "",
                            secondaryLabels = fakeModel.find { it.id == tableId }?.tableHeaderModel?.rows?.map {
                                it.cells[cell.column!! % it.cells.size].value
                            } ?: emptyList(),
                            currentValue = cell.value,
                            keyboardInputType = KeyboardInputType.TextInput(),
                            error = null
                        )
                    } else {
                        null
                    }
                },
                onEdition = {},
                onCellValueChange = { tableCell ->
                    val updatedData = fakeModel.map { tableModel ->
                        val hasRowWithDataElement = tableModel.tableRows.find {
                            tableCell.id?.contains(it.rowHeader.id.toString()) == true
                        }
                        if (hasRowWithDataElement != null) {
                            tableModel.copy(
                                overwrittenValues = mapOf(
                                    Pair(tableCell.column!!, tableCell)
                                )
                            )
                        } else {
                            tableModel
                        }
                    }
                    model = TableScreenState(updatedData, false)
                },
                onSaveValue = { tableCell, selectNext ->
                    onSaveTableCell = tableCell
                    onSave(tableCell)
                    model = TableScreenState(fakeModel, selectNext)
                }
            )
        }
        return fakeModel
    }

    fun assertClickOnCellShouldOpenInputComponent(tableId: String,rowIndex: Int, columnIndex: Int) {
        clickOnCell(tableId, rowIndex, columnIndex)
        composeTestRule.waitForIdle()
        assertInputComponentIsDisplayed()
    }

    fun assertClickOnEditOpensInputKeyboard() {
        clickOnEditionIcon()
        composeTestRule.waitForIdle()
        assertKeyBoardVisibility(true)
        assertInputIcon(R.drawable.ic_finish_edit_input)
    }

    fun assertClickOnBackClearsFocus() {
        pressBack()
        composeTestRule.waitForIdle()
        assertInputIcon(R.drawable.ic_edit_input)
    }

    fun assertClickOnSaveHidesKeyboardAndSaveValue(valueToType: String) {
        clearInput()
        composeTestRule.waitForIdle()
        typeInput(valueToType)
        composeTestRule.waitForIdle()
        clickOnAccept()
    }

    fun assertInfoIcon(tableId: String, rowIndex: Int) {
        composeTestRule.onNode(
            SemanticsMatcher.expectValue(TableId, tableId)
                .and(SemanticsMatcher.expectValue(RowIndex, rowIndex))
                .and(SemanticsMatcher.expectValue(InfoIconId, INFO_ICON))
        ).assertExists()
    }

    fun assertRowHeaderBackgroundChangeToPrimary(
        tableId: String,
        rowIndex: Int,
        tableColors: TableColors
    ) {
        composeTestRule.onNode(
            SemanticsMatcher.expectValue(TableId, tableId)
                .and(SemanticsMatcher.expectValue(RowIndex, rowIndex))
                .and(SemanticsMatcher.expectValue(RowBackground, tableColors.primary))
        ).assertExists()
    }

    fun assertColumnHeaderBackgroundColor(
        tableId: String,
        rowIndex: Int,
        columnIndex: Int,
        color: Color
    ) {
        composeTestRule.onNode(
            SemanticsMatcher.expectValue(TableIdColumnHeader, tableId)
                .and(SemanticsMatcher.expectValue(RowIndexHeader, rowIndex))
                .and(SemanticsMatcher.expectValue(ColumnIndexHeader, columnIndex))
                .and(SemanticsMatcher.expectValue(ColumnBackground, color))
        ).assertExists()
    }

    fun clickOnCell(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNodeWithTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex", true)
            .performScrollTo()
        composeTestRule.onNodeWithTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex", true)
            .performClick()
    }

    fun clickOnHeaderElement(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNodeWithTag("$HEADER_CELL$tableId$rowIndex$columnIndex", true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickOnRowHeader(tableId: String, rowIndex: Int) {
        composeTestRule.onNodeWithTag("$tableId$rowIndex").performClick()
        composeTestRule.waitForIdle()
    }

    fun assertRowHeaderText(tableId: String, text: String, rowIndex: Int) {
        composeTestRule.onNodeWithTag("${tableId}${rowIndex}").assertTextEquals(text)
    }

    fun assertRowHeaderIsClickable(tableId: String, text: String, rowIndex: Int) {
        composeTestRule.onNodeWithTag("$tableId$rowIndex").assertIsEnabled()
    }

    fun clickOnEditValue() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performClick()
    }

    fun clickOnEditionIcon() {
        composeTestRule.onNodeWithTag(INPUT_ICON_TEST_TAG).performClick()
    }

    fun clearInput() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performTextClearance()
    }

    fun typeInput(text: String) {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performTextInput(text)
    }

    fun assertBottomBarIsVisible() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).assertIsDisplayed()
    }

    fun assertBottomBarIsNotVisible() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).assertIsNotDisplayed()
    }

    fun clickOnAccept() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performImeAction()
    }

    fun assertInputComponentIsDisplayed() {
        composeTestRule.onNodeWithTag(INPUT_TEST_TAG).assertIsDisplayed()
    }

    fun assertInputIcon(@DrawableRes id: Int) {
        composeTestRule.onNode(SemanticsMatcher.expectValue(DrawableId, id), true)
            .assertIsDisplayed()
    }

    fun assertIconIsVisible(@DrawableRes id: Int) {
        composeTestRule.onNode(SemanticsMatcher.expectValue(DrawableId, id)).assertIsDisplayed()
    }

    fun assertOnSavedTableCellValue(value: String) {
        Assert.assertEquals(value, onSaveTableCell.value)
    }

    private fun assertInputComponentErrorMessageIsDisplayed(expectedErrorMessage: String) {
        composeTestRule.onNodeWithTag(INPUT_ERROR_MESSAGE_TEST_TAG)
            .assertIsDisplayed()
            .assertTextEquals(expectedErrorMessage)
    }

    fun assertCellWithErrorSetsErrorMessage(
        rowIndex: Int,
        columnIndex: Int,
        expectedErrorMessage: String
    ) {
        clickOnCell("table", rowIndex, columnIndex)
        assertInputComponentIsDisplayed()
        assertInputComponentIsDisplayed()
        assertInputComponentErrorMessageIsDisplayed(expectedErrorMessage)
    }

    fun assertCellHasMandatoryIcon(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNode(
            hasParent(hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex"))
                    and
                    hasTestTag(MANDATORY_ICON_TEST_TAG), true
        )
            .assertIsDisplayed()
    }

    fun typeOnInputComponent(valueToType: String) {
        clickOnEditValue()
        typeInput(valueToType)
    }

    fun assertCellHasText(tableId: String, rowIndex: Int, columnIndex: Int, expectedValue: String) {
        composeTestRule.onNode(
            hasParent(hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex"))
                    and
                    hasTestTag(CELL_VALUE_TEST_TAG),
            true
        ).assertTextEquals(expectedValue)
    }

    fun assertCellSelected(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNode(
            hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex"), true
        ).assertIsDisplayed()
        composeTestRule.onNode(
            hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex")
                    and
                    SemanticsMatcher.expectValue(CellSelected, true), true
        ).assertIsDisplayed()
    }

    fun assertNoCellSelected() {
        composeTestRule.onNode(SemanticsMatcher.expectValue(CellSelected, true))
            .assertDoesNotExist()
    }

    fun assertInputComponentInfo(expectedMainLabel: String, expectedSecondaryLabels: String) {
        composeTestRule.onNode(
            hasParent(hasTestTag(INPUT_TEST_TAG))
                    and
                    SemanticsMatcher.expectValue(MainLabel, expectedMainLabel)
                    and
                    SemanticsMatcher.expectValue(SecondaryLabels, expectedSecondaryLabels),
            true
        ).assertExists()
    }

    fun assertInputComponentIsHidden() {
        composeTestRule.onNodeWithTag(INPUT_TEST_TAG, true).assertIsNotDisplayed()
    }

    fun assertUnselectedCellErrorStyle(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNode(
            hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex")
                    and
                    SemanticsMatcher.expectValue(HasError, true), true
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CELL_ERROR_UNDERLINE_TEST_TAG, true).assertIsDisplayed()
    }

    fun assertSelectedCellErrorStyle(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNode(
            hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex")
                    and
                    SemanticsMatcher.expectValue(HasError, true), true
        ).assertIsDisplayed()
    }

    fun assertCellBlockedCell(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule
            .onNode(
                hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex")
                        and
                        SemanticsMatcher.expectValue(IsBlocked, true),
                true
            )
            .assertIsDisplayed()
    }

    fun assertKeyBoardVisibility(visibility: Boolean) {
        keyboardHelper.waitForKeyboardVisibility(visibility)
    }

    fun hideKeyboard() {
        keyboardHelper.hideKeyboardIfShown()
    }
}