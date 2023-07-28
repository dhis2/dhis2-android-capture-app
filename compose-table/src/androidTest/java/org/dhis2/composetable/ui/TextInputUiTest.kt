package org.dhis2.composetable.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.data.input_error_message
import org.dhis2.composetable.data.tableData
import org.dhis2.composetable.model.FakeModelType
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.tableRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class TextInputUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun validateTextInputRequirements() {
        var cellToSave: TableCell? = null
        val expectedValue = "55"

        tableRobot(composeTestRule) {
            val fakeModels = initTableAppScreen(
                FakeModelType.MULTIHEADER_TABLE,
                onSave = { cellToSave = it }
            )
            val tableId = fakeModels[0].id!!
            assertClickOnCellShouldOpenInputComponent(tableId, 0, 0)
            assertClickOnBackClearsFocus()
            assertClickOnEditOpensInputKeyboard()
            assertClickOnSaveHidesKeyboardAndSaveValue(expectedValue)
            assert(cellToSave?.value == expectedValue)
        }
    }

    @Test
    fun shouldSetCorrectColorAndMessageIfHasError() {
        composeTestRule.setContent {
            TextInputUiTestScreen { }
        }

        tableRobot(composeTestRule) {
            assertCellWithErrorSetsErrorMessage(0, 1, input_error_message)
        }
    }

    @Ignore("It is not deterministic, to fix on https://dhis2.atlassian.net/browse/ANDROAPP-4987")
    @Test
    fun shouldClearFocusWhenKeyboardIsHidden() {
        tableRobot(composeTestRule) {
            val fakeModels = initTableAppScreen(
                FakeModelType.MANDATORY_TABLE
            )
            clickOnCell(fakeModels.first().id!!, 0, 0)
            assertInputComponentIsDisplayed()
            assertClickOnBackClearsFocus()
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun TextInputUiTestScreen(onSave: (TableCell) -> Unit) {
        val bottomSheetState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
        )
        var currentCell by remember {
            mutableStateOf<TableCell?>(
                null
            )
        }
        var currentInputType by remember {
            mutableStateOf(
                TextInputModel()
            )
        }

        var tableSelection by remember {
            mutableStateOf<TableSelection>(TableSelection.Unselected())
        }

        val coroutineScope = rememberCoroutineScope()

        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetContent = {
                TextInput(
                    textInputModel = currentInputType,
                    onTextChanged = { textInputModel ->
                        currentInputType = textInputModel
                        currentCell = currentCell?.copy(
                            value = textInputModel.currentValue,
                            error = null
                        )
                    },
                    onSave = { currentCell?.let { onSave(it) } },
                    onNextSelected = {},
                    focusRequester = FocusRequester()
                )
            },
            sheetPeekHeight = 0.dp,
            sheetShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp
            )
        ) {
            CompositionLocalProvider(
                LocalTableSelection provides tableSelection
            ) {
                DataTable(
                    tableList = tableData,
                    tableInteractions = object : TableInteractions {
                        override fun onSelectionChange(newTableSelection: TableSelection) {
                            tableSelection = newTableSelection
                        }

                        override fun onClick(tableCell: TableCell) {
                            currentCell = tableCell
                            currentInputType = TextInputModel(
                                id = tableCell.id!!,
                                mainLabel = "Main Label",
                                secondaryLabels = listOf("Second Label 1", "Second Label 2"),
                                tableCell.value,
                                error = currentCell?.error
                            )
                            coroutineScope.launch {
                                if (bottomSheetState.bottomSheetState.isCollapsed) {
                                    bottomSheetState.bottomSheetState.expand()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
