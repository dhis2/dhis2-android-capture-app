package org.dhis2.composetable

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.model.FakeModelType
import org.junit.Rule
import org.junit.Test

class BottomBarTableTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()

    @Test
    fun shouldDisplayBottomBarComponentWhenTyping() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
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
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("test")
            composeTestRule.waitForIdle()
            clickOnAccept()
            assertCellHasText(tableId, 1, 0, "test")
        }
    }

    @Test
    fun shouldAssertBottomBarStateBeforeAndAfterTyping() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            assertIconIsVisible(R.drawable.ic_finish_edit_input)
            typeOnInputComponent("test")
            clickOnEditionIcon()
            assertIconIsVisible(R.drawable.ic_edit_input)
            composeTestRule.waitForIdle()
            assertCellHasText(tableId, 1, 0, "test")
        }
    }

    @Test
    fun shouldClickOnNextAndSavedValue() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(
                composeTestRule.activity.applicationContext,
                FakeModelType.MANDATORY_TABLE
            )
            val tableId = fakeModel[0].id
            clickOnCell(tableId!!, 1, 0)
            composeTestRule.waitForIdle()
            typeOnInputComponent("test")
            clickOnAccept()
            composeTestRule.waitForIdle()
            assertCellHasText(tableId, 1, 0, "test")
            assertOnSavedTableCellValue("test")
        }
    }
}
