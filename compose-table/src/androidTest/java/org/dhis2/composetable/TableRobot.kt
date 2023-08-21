package org.dhis2.composetable

import androidx.annotation.DrawableRes
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.IOException
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.actions.TableResizeActions
import org.dhis2.composetable.data.TableAppScreenOptions
import org.dhis2.composetable.model.FakeModelType
import org.dhis2.composetable.model.FakeTableModels
import org.dhis2.composetable.model.KeyboardInputType
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.DataTable
import org.dhis2.composetable.ui.DrawableId
import org.dhis2.composetable.ui.INPUT_ERROR_MESSAGE_TEST_TAG
import org.dhis2.composetable.ui.INPUT_ICON_TEST_TAG
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.composetable.ui.INPUT_TEST_TAG
import org.dhis2.composetable.ui.LocalTableSelection
import org.dhis2.composetable.ui.MainLabel
import org.dhis2.composetable.ui.SecondaryLabels
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableConfiguration
import org.dhis2.composetable.ui.TableSelection
import org.dhis2.composetable.ui.TableTheme
import org.dhis2.composetable.ui.compositions.LocalInteraction
import org.dhis2.composetable.ui.semantics.CELL_ERROR_UNDERLINE_TEST_TAG
import org.dhis2.composetable.ui.semantics.CELL_TEST_TAG
import org.dhis2.composetable.ui.semantics.CELL_VALUE_TEST_TAG
import org.dhis2.composetable.ui.semantics.CellSelected
import org.dhis2.composetable.ui.semantics.ColumnBackground
import org.dhis2.composetable.ui.semantics.ColumnIndexHeader
import org.dhis2.composetable.ui.semantics.HEADER_CELL
import org.dhis2.composetable.ui.semantics.HasError
import org.dhis2.composetable.ui.semantics.INFO_ICON
import org.dhis2.composetable.ui.semantics.InfoIconId
import org.dhis2.composetable.ui.semantics.IsBlocked
import org.dhis2.composetable.ui.semantics.MANDATORY_ICON_TEST_TAG
import org.dhis2.composetable.ui.semantics.RowBackground
import org.dhis2.composetable.ui.semantics.RowIndex
import org.dhis2.composetable.ui.semantics.RowIndexHeader
import org.dhis2.composetable.ui.semantics.TableId
import org.dhis2.composetable.ui.semantics.TableIdColumnHeader
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
        fakeModelType: FakeModelType,
        tableColors: TableColors = TableColors(),
        onSave: (TableCell) -> Unit = {}
    ): List<TableModel> {
        var fakeModel: List<TableModel> = emptyList()
        composeTestRule.setContent {
            fakeModel = FakeTableModels(LocalContext.current).getMultiHeaderTables(fakeModelType)
            var tableSelection by remember {
                mutableStateOf<TableSelection>(TableSelection.Unselected())
            }
            TableTheme(
                tableColors = TableColors().copy(primary = MaterialTheme.colors.primary),
                tableConfiguration = TableConfiguration(headerActionsEnabled = false),
                tableResizeActions = object : TableResizeActions {}
            ) {
                val iteractions = object : TableInteractions {
                    override fun onSelectionChange(newTableSelection: TableSelection) {
                        tableSelection = newTableSelection
                    }
                }
                CompositionLocalProvider(
                    LocalTableSelection provides tableSelection,
                    LocalInteraction provides iteractions
                ) {
                    DataTable(
                        tableList = fakeModel
                    )
                }
            }
        }
        return fakeModel
    }

    fun initTableAppScreen(
        fakeModelType: FakeModelType,
        tableAppScreenOptions: TableAppScreenOptions = TableAppScreenOptions(),
        tableConfiguration: TableConfiguration = TableConfiguration(headerActionsEnabled = true),
        onSave: (TableCell) -> Unit = {}
    ): List<TableModel> {
        var fakeModel: List<TableModel> = emptyList()
        composeTestRule.setContent {
            fakeModel = FakeTableModels(LocalContext.current).getMultiHeaderTables(fakeModelType)
            val screenState = TableScreenState(fakeModel)

            keyboardHelper.view = LocalView.current
            var model by remember { mutableStateOf(screenState) }
            TableTheme(
                tableColors = TableColors().copy(primary = MaterialTheme.colors.primary),
                tableConfiguration = tableConfiguration,
                tableResizeActions = object : TableResizeActions {}
            ) {
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
                    onSaveValue = { tableCell ->
                        onSaveTableCell = tableCell
                        onSave(tableCell)
                        val updatedData = updateValue(fakeModel, tableCell)
                        model = TableScreenState(updatedData)
                    }
                )
            }
        }
        return fakeModel
    }

    private fun updateValue(fakeModel: List<TableModel>, tableCell: TableCell): List<TableModel> {
        return fakeModel.map { tableModel ->
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
    }

    fun assertClickOnCellShouldOpenInputComponent(
        tableId: String,
        rowIndex: Int,
        columnIndex: Int
    ) {
        clickOnCell(tableId, rowIndex, columnIndex)
        composeTestRule.waitForIdle()
        assertInputComponentIsDisplayed()
    }

    fun assertClickOnEditOpensInputKeyboard() {
        clickOnEditionIcon()
        composeTestRule.waitForIdle()
        val checkKeyboardCmd = "dumpsys input_method | grep mInputShown"
        try {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val keyboardOpened = device.executeShellCommand(checkKeyboardCmd)
                .contains("mInputShown=true")
            Assert.assertTrue(keyboardOpened)
        } catch (e: IOException) {
            throw RuntimeException("Keyboard check failed", e)
        }
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

    fun clickOnBack() {
        pressBack()
    }

    fun assertInputComponentIsDisplayed() {
        composeTestRule.onNodeWithTag(INPUT_TEST_TAG).assertIsDisplayed()
    }

    fun assertInputIcon(@DrawableRes id: Int) {
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(SemanticsMatcher.expectValue(DrawableId, id), true)
                .fetchSemanticsNodes().size == 1
        }
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
        )
            .performScrollTo()
            .assertIsDisplayed()
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
        keyboardHelper.hideKeyboard()
    }
}