package org.dhis2.usescases.datasets

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.composetable.ui.semantics.CELL_TEST_TAG
import org.dhis2.composetable.ui.semantics.CellSelected
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_TAG
import org.dhis2.mobile.aggregates.ui.constants.SYNC_BUTTON_TAG
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.cellTestTag
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.headersTestTag
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.rowHeaderTestTag
import org.junit.Assert.assertTrue

fun dataSetTableRobot(
    composeTestRule: ComposeContentTestRule,
    dataSetTableRobot: DataSetTableRobot.() -> Unit
) {
    DataSetTableRobot(composeTestRule).apply {
        dataSetTableRobot()
    }
}

class DataSetTableRobot(
    private val composeTestRule: ComposeContentTestRule
) : BaseRobot() {

    fun clickOnSaveButton() {
        waitForView(withId(R.id.saveButton)).perform(click())
    }

    fun clickOnPositiveButton() {
        onView(withId(R.id.positive)).perform(click())
    }

    fun clickOnNegativeButton() {
        onView(withId(R.id.negative)).perform(click())
    }

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnMenuReOpen() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.onNodeWithText(getString(R.string.re_open)).performClick()
        }
    }

    fun clickOnCell(tableId: String, cellId: String) {
        composeTestRule.onNodeWithTag(cellTestTag(tableId, cellId), useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
    }

    @Deprecated("This won't work for the design system table")
    fun clickOnCell(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNodeWithTag("$tableId$CELL_TEST_TAG$rowIndex$columnIndex", true)
            .performScrollTo()
            .performClick()
    }

    fun assertCellSelected(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNode(
            hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex")
                    and
                    SemanticsMatcher.expectValue(CellSelected, true), true
        ).assertIsDisplayed()
    }

    fun clickOnEditValue() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performClick()
    }

    fun typeInput(text: String) {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performTextInput(text)
    }

    fun clickOnAccept() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performImeAction()
    }

    fun checkActivityHasNotFinished(activity: DataSetTableActivity) {
        assertTrue(!activity.isDestroyed)
    }

    fun syncIsAvailable() {
        composeTestRule.onNodeWithTag(SYNC_BUTTON_TAG)
            .assertIsDisplayed()
            .performClick()

        //TODO: When sync is implemented add new assertions
    }

    fun indicatorTableIsDisplayed() {
        assertTableIsDisplayed()

        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .performScrollToIndex(22)
        composeTestRule.onNodeWithText("Moderate malnutrition rate", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .performScrollToIndex(0)
    }

    fun totalsAreDisplayed(
        tableId: String,
        totalColumnHeaderRowIndex: Int,
        totalColumnHeaderColumnIndex: Int,
    ) {
        composeTestRule.onNodeWithTag("HEADER_CELL$tableId$totalColumnHeaderRowIndex$totalColumnHeaderColumnIndex")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(headersTestTag(tableId))
            .performTouchInput {
                swipeRight()
            }

        composeTestRule.onNodeWithTag(rowHeaderTestTag(tableId, "${tableId}_totals"))
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .performScrollToIndex(0)
    }

    fun clickOnAcceptDate() {
        onView(withText(R.string.action_accept)).perform(click())
    }

    fun assertInputDialogIsDisplayed(composeTestRule: ComposeContentTestRule) {
        composeTestRule.onNodeWithTag(INPUT_DIALOG_TAG).assertIsDisplayed()
    }

    fun assertInputDescriptionIsDisplayed(description: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(INPUT_DIALOG_TAG).printToLog("INPUT_DIALOG_TAG")
        composeTestRule.onNodeWithText(description, useUnmergedTree = true).assertIsDisplayed()
    }

    fun typeOnInputDialog(value: String) {
        composeTestRule.onNodeWithTag("INPUT_INTEGER_FIELD").performTextInput(value)
    }

    fun assertCellHasValue(
        tableId: String,
        cellId: String,
        expectedValue: String
    ) {
        assertTableIsDisplayed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(cellTestTag(tableId, cellId), true)
            .onChild().assertTextEquals(expectedValue)
    }

    fun assertRowTotalValue(
        tableId: String,
        rowIndex: Int,
        expectedValue: String,
    ) {
        composeTestRule.onNodeWithTag("HEADER_CELL${tableId}12")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onRoot().printToLog("WHERE IS THE TOTALS")

        composeTestRule.onNodeWithTag("CELL_TEST_TAG_${tableId}${tableId}_${rowIndex}_totals", true)
            .onChild().assertTextEquals(expectedValue)

        composeTestRule.onNodeWithTag(headersTestTag(tableId))
            .performTouchInput {
                swipeRight()
            }
        composeTestRule.waitForIdle()
    }

    fun assertColumnTotalValue(
        tableId: String,
        columnIndex: Int,
        expectedValue: String,
    ) {
        composeTestRule.onNodeWithTag(rowHeaderTestTag(tableId, "${tableId}_totals"))
            .performScrollTo()

        composeTestRule.onNodeWithTag("CELL_TEST_TAG_${tableId}${tableId}_totals_$columnIndex", true)
            .onChild().assertTextEquals(expectedValue)

        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .performScrollToIndex(0)

        composeTestRule.waitForIdle()
    }

    fun returnToDataSetInstanceList() {
        composeTestRule.onNodeWithContentDescription("back arrow")
            .performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertTableIsDisplayed() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag("TABLE_SCROLLABLE_COLUMN"),
            timeoutMillis = 3000
        )
    }
}
