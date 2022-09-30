package org.dhis2.composetable

import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.unit.IntSize
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.data.InputRowOption
import org.dhis2.composetable.data.TableAppScreenOptions
import org.dhis2.composetable.model.FakeModelType
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.ui.INPUT_TEST_TAG
import org.dhis2.composetable.utils.KeyboardHelper
import org.junit.Rule
import org.junit.Test

class CellTableTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()

    @Test
    fun shouldDisplayMandatoryIcon() {
        tableRobot(composeTestRule) {
            val fakeModel = initTable(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 0, 0)
            assertCellHasMandatoryIcon(firstId, 0, 0)
        }
    }

    @Test
    fun shouldBlockClickAndSetCorrectColorIfNonEditable() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 0, 1)
            assertInputComponentIsHidden()
            assertCellBlockedCell(firstId, 0, 1)
        }
    }

    @Test
    fun shouldUpdateValueWhenTypingInComponent() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 1, 0)
            typeOnInputComponent("check")
            assertCellHasText(firstId, 1, 0, "check")
        }
    }

    @Test
    fun shouldSaveValue() {
        var savedValue: TableCell? = null
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            ) {
                savedValue = it
            }
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 1, 0)
            typeOnInputComponent("check")
            clickOnCell(firstId, 1, 2)
            assert(savedValue != null)
        }
    }

    @Test
    fun shouldMoveToNextColumnWhenClickingNext() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 0, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("check")
            composeTestRule.waitForIdle()
            clickOnAccept()
            composeTestRule.waitForIdle()
            assertCellSelected(firstId, 0, 2)
            assertInputComponentInfo(
                expectedMainLabel = "Text 1",
                expectedSecondaryLabels = fakeModel.find { it.id == firstId }?.tableHeaderModel?.rows
                    ?.joinToString(separator = ",") { it.cells[2 % it.cells.size].value } ?: ""
            )
        }
    }

    @Test
    fun shouldMoveToNextRowWhenClickingNext() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 0, 47)
            composeTestRule.waitForIdle()
            typeOnInputComponent("check")
            composeTestRule.waitForIdle()
            clickOnAccept()
            composeTestRule.waitForIdle()
            assertCellSelected(firstId, 1, 0)
            assertInputComponentInfo(
                expectedMainLabel = "Text 2",
                expectedSecondaryLabels =
                fakeModel.find { it.id == firstId }?.tableHeaderModel?.rows
                    ?.joinToString(separator = ",") { it.cells[0 % it.cells.size].value } ?: "")
        }
    }

    @Test
    fun shouldClearSelectionWhenClickingNextOnLastCell() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 2, 47)
            typeOnInputComponent("check")
            clickOnAccept()
            assertNoCellSelected()
            assertInputComponentIsHidden()
        }
    }

    @Test
    fun shouldHideInputComponentIfSelectedCellDoesNotRequireIt() {
        val testingTableId = "PjKGwf9WxBE"
        tableRobot(composeTestRule) {
            initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE,
                TableAppScreenOptions(
                    inputRowsOptions = listOf(
                        InputRowOption(tableId = testingTableId, 1, false)
                    )
                )
            )
            clickOnCell(testingTableId, 0, 0)
            assertKeyBoardVisibility(true)
            assertInputComponentIsDisplayed()
            clickOnCell(testingTableId, 1, 0)
            assertKeyBoardVisibility(false)
            assertInputComponentIsHidden()
        }
    }

    @Test
    fun shouldBlockSelectingNewCellIfCurrentHasError() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val firstId = fakeModel.first().id!!
            clickOnCell(firstId, 2, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("")
            composeTestRule.waitForIdle()
            clickOnAccept()
            composeTestRule.waitForIdle()
            assertCellSelected(firstId, 2, 0)
        }
    }

    @Test
    fun shouldSetCorrectColorIfHasError() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val firstId = fakeModel.first().id!!
            assertUnselectedCellErrorStyle(firstId, 2, 0)
            clickOnCell(firstId, 2, 0)
            assertSelectedCellErrorStyle(firstId, 2, 0)
        }
    }
}