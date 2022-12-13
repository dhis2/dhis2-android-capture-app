package org.dhis2.composetable

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.data.InputRowOption
import org.dhis2.composetable.data.TableAppScreenOptions
import org.dhis2.composetable.model.FakeModelType
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableTheme
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

    //tests  - examples
    // omprobar que cuando haces click en una celda no hay error
    @Test
    fun shouldNotDisplayError() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )

            val firstId = fakeModel.first().id!!
            val cellColor = TableColors()
            clickOnCell(firstId, 2, 2)
            assertInputComponentIsDisplayed()
            assertSelectedCellWithoutErrorStyle(firstId,2,2)
        }
    }
    // Hacer que falle el test anterior, si el valor de column fuese 0, entonces si pasaria el test

    @Test
    fun shouldDisplayError() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )

            val firstId = fakeModel.first().id!!
            val cellColor = TableColors()
            clickOnCell(firstId, 2, 2)
            assertInputComponentIsDisplayed()
            assertSelectedCellErrorStyle(firstId,2,2)
        }
    }
// Comprobar que cuando haces click en una celda, el background cambia a color azul y el bottom bar sea visible
    @Test
    fun shouldSetCorrectColorIfNotError() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen2(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )

            val firstId = fakeModel.second.first().id!!
            val cellColor = TableColors()
            clickOnCell(firstId, 2, 2)
            assertSelectedCellBorderStyle(firstId,2,2, cellColor.primary)
        }
    }


}