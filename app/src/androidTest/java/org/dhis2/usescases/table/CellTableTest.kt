package org.dhis2.usescases.table

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.test.TestActivity
import org.dhis2.usescases.BaseTest
import org.junit.Rule
import org.junit.Test

class CellTableTest : BaseTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

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
            composeTestRule.waitForIdle()
            assertInputComponentInfo(
                expectedMainLabel = fakeModel.first().tableRows[0].rowHeader.title,
                expectedSecondaryLabels = fakeModel.first().tableHeaderModel.rows.first().cells[1].value
            )
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