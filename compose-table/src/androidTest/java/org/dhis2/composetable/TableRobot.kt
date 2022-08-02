package org.dhis2.composetable

import androidx.annotation.DrawableRes
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.dhis2.composetable.ui.CELL_TEST_TAG
import org.dhis2.composetable.ui.DrawableId
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.composetable.ui.INPUT_TEST_TAG

fun tableRobot(
    composeTestRule: ComposeContentTestRule,
    tableRobot: TableRobot.() -> Unit
) {
    TableRobot(composeTestRule).apply {
        tableRobot()
    }
}

class TableRobot(
    private val composeTestRule: ComposeContentTestRule,
) {

    fun assertClickOnCellShouldOpenInputComponent(rowIndex: Int, columnIndex: Int) {
        clickOnCell("table", rowIndex, columnIndex)
        assertInputComponentIsDisplayed()
    }

    fun assertClickOnEditOpensInputKeyboard() {
        clickOnEditValue()
        assertInputIcon(R.drawable.ic_finish_edit_input)
    }


    fun assertClickOnSaveHidesKeyboardAndSaveValue(valueToType: String) {
        clearInput()
        typeInput(valueToType)
        clickOnAccept()
    }

    fun clickOnCell(tableId:String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNodeWithTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex", true).performClick()
    }

    fun clickOnRowHeader(tableId: String, rowIndex: Int){
        composeTestRule.onNodeWithTag("$tableId$rowIndex").performClick()
    }

    fun assertRowHeaderText(tableId: String, text: String, rowIndex: Int){
        composeTestRule.onNodeWithTag("$tableId$rowIndex").assertTextEquals(text)
    }

    fun assertRowHeaderIsClickable(tableId: String, text: String, rowIndex: Int){
        composeTestRule.onNodeWithTag("$tableId$rowIndex").assertIsEnabled()
    }

    private fun clickOnEditValue() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performClick()
    }

    private fun clearInput() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performTextClearance()
    }

    private fun typeInput(text: String) {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performTextInput(text)
    }

    private fun clickOnAccept() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performImeAction()
    }

    private fun assertInputComponentIsDisplayed() {
        composeTestRule.onNodeWithTag(INPUT_TEST_TAG).assertIsDisplayed()
    }

    private fun assertInputIcon(@DrawableRes id: Int) {
        composeTestRule.onNode(SemanticsMatcher.expectValue(DrawableId, id)).assertExists()
    }
}