package org.dhis2.composetable

import androidx.compose.ui.test.junit4.createComposeRule
import org.dhis2.composetable.model.FakeModelType
import org.dhis2.composetable.ui.TableConfiguration
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class BottomBarTableTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayBottomBarComponentWhenTyping() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                FakeModelType.MULTIHEADER_TABLE
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            typeOnInputComponent("check")
            assertBottomBarIsVisible()
        }
    }

    @Test
    fun shouldTheElementWrittenInBottomBarBeTheSameInCell() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                FakeModelType.MANDATORY_TABLE
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("test")
            composeTestRule.waitForIdle()
            composeTestRule.waitForIdle()
            assertCellHasText(tableId, 1, 0, "test")
        }
    }

    @Test
    fun shouldAssertBottomBarStateBeforeAndAfterTyping() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                FakeModelType.MANDATORY_TABLE
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            assertIconIsVisible(R.drawable.ic_finish_edit_input)
            typeOnInputComponent("test")
            assertCellHasText(tableId, 1, 0, "test")
            clickOnEditionIcon()
            assertIconIsVisible(R.drawable.ic_edit_input)
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun shouldClickOnNextAndSavedValue() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                FakeModelType.MANDATORY_TABLE
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("test")
            assertCellHasText(tableId, 1, 0, "test")
            clickOnAccept()
            composeTestRule.waitForIdle()
            assertOnSavedTableCellValue("test")
        }
    }

    @Ignore
    @Test
    fun shouldHideInputFieldIfTextInputViewModeIsOff() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                fakeModelType = FakeModelType.MANDATORY_TABLE,
                tableConfiguration = TableConfiguration(
                    headerActionsEnabled = false,
                    textInputViewMode = false
                )
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("test")
            assertCellHasText(tableId, 1, 0, "test")
            clickOnBack()
            composeTestRule.waitForIdle()
            assertBottomBarIsNotVisible()
        }
    }

    @Test
    fun shouldShowInputFieldIfTextInputViewModeIsOn() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                fakeModelType = FakeModelType.MANDATORY_TABLE,
                tableConfiguration = TableConfiguration(
                    headerActionsEnabled = false,
                    textInputViewMode = true
                )
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("test")
            assertCellHasText(tableId, 1, 0, "test")
            clickOnBack()
            composeTestRule.waitForIdle()
            assertBottomBarIsVisible()
        }
    }
}
