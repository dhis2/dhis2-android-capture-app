package org.dhis2.composetable

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.data.tableData
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.ui.DataTable
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TextInput
import org.junit.Rule
import org.junit.Test

class TextInputUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()

    @Test
    fun validateTextInputRequirements() {
        var cellToSave: TableCell? = null
        val expectedValue = "55"

        composeTestRule.setContent {
            TextInputUiTestScreen {
                cellToSave = it
            }
        }

        tableRobot(composeTestRule) {
            assertClickOnCellShouldOpenInputComponent(0, 0)
            assertClickOnEditOpensInputKeyboard()
            assertClickOnSaveHidesKeyboardAndSaveValue(expectedValue)
            assert(cellToSave?.value == expectedValue)
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
        val coroutineScope = rememberCoroutineScope()

        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetContent = {
                TextInput(
                    textInputModel = currentInputType,
                    onTextChanged = {

                    },
                    onSave = { textInputModel ->
                        currentCell?.copy(value = textInputModel.currentValue)?.let {
                            onSave(it)
                        }
                    }
                )
            },
            sheetPeekHeight = 0.dp,
            sheetShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp
            )
        ) {
            DataTable(
                tableList = tableData,
                tableColors = TableColors(
                    primary = MaterialTheme.colors.primary,
                    primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                )
            ) { cell ->
                currentCell = cell
                currentInputType = TextInputModel(
                    id = cell.id!!,
                    mainLabel = "Main Label",
                    secondaryLabels = listOf("Second Label 1", "Second Label 2"),
                    cell.value
                )
                coroutineScope.launch {
                    if (bottomSheetState.bottomSheetState.isCollapsed) {
                        bottomSheetState.bottomSheetState.expand()
                    }
                }
            }
        }
    }
}