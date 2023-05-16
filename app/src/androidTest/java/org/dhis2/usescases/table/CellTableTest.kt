package org.dhis2.usescases.table

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.test.TestActivity
import org.dhis2.usescases.BaseTest
import org.junit.Rule
import org.junit.Test

class CellTableTest : BaseTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun shouldBlockClickAndSetCorrectColorIfNonEditable() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen()
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 0, 1)
            assertInputComponentIsHidden()
            assertCellBlockedCell(firstId, 0, 1)
        }
    }

    @Test
    fun shouldUpdateValueWhenTypingInComponent() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen()
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 1, 0)
            typeOnInputComponent("check")
            assertCellHasText(firstId, 1, 0, "check")
        }
    }

    @Test
    fun shouldMoveToNextColumnWhenClickingNext() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen()
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 0, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("check")
            composeTestRule.waitForIdle()
            clickOnAccept()
            composeTestRule.waitForIdle()
            assertCellSelected(firstId, 0, 1)
            assertInputComponentInfo(
                expectedMainLabel = fakeModel.first().tableRows[0].rowHeader.title,
                expectedSecondaryLabels = fakeModel.first().tableHeaderModel.rows.first().cells[1].value
            )
        }
    }

    @Test
    fun shouldMoveToNextRowWhenClickingNext() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen()
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 0, 47)
            composeTestRule.waitForIdle()
            typeOnInputComponent("check")
            composeTestRule.waitForIdle()
            clickOnAccept()
            composeTestRule.waitForIdle()
            Espresso.pressBack()
            assertCellSelected(firstId, 1, 0)
            clickOnCell(firstId, 1, 0)
            assertInputComponentInfo(
                expectedMainLabel = "Text 2",
                expectedSecondaryLabels =
                fakeModel.find { it.id == firstId }?.tableHeaderModel?.rows
                    ?.joinToString(separator = ",") { it.cells[0 % it.cells.size].value } ?: ""
            )
        }
    }

    @Test
    fun shouldClearSelectionWhenClickingNextOnLastCell() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen()
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 2, 47)
            typeOnInputComponent("check")
            clickOnAccept()
            assertNoCellSelected()
            assertInputComponentIsHidden()
        }
    }

    @Test
    fun shouldSetCorrectColorIfHasError() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen()
            val firstId = fakeModel.first().id!!
            assertUnselectedCellErrorStyle(firstId, 2, 0)
            clickOnCell(firstId, 2, 0)
            assertSelectedCellErrorStyle(firstId, 2, 0)
        }
    }

    @Test
    fun validateTextInputRequirements() {
        var cellToSave: TableCell? = null
        val expectedValue = "55"

        tableRobot(composeTestRule) {
            val fakeModels = initTableAppScreen(
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
}