package org.dhis2.usescases.datasets

import androidx.compose.ui.semantics.SemanticsProperties.TestTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.composetable.ui.semantics.CELL_TEST_TAG
import org.dhis2.composetable.ui.semantics.CellSelected
import org.dhis2.mobile.aggregates.ui.constants.COMPLETION_DIALOG_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_DONE_TAG
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_TAG
import org.dhis2.mobile.aggregates.ui.constants.MANDATORY_FIELDS_DIALOG_OK_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.OPTIONAL_VALIDATION_RULE_DIALOG_ACCEPT_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.SAVE_BUTTON_TAG
import org.dhis2.mobile.aggregates.ui.constants.SYNC_BUTTON_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_BAR_EXPAND_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_BAR_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_DIALOG_COMPLETE_ANYWAY_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_DIALOG_REVIEW_BUTTON_TEST_TAG
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.TEST_TAG_COLUMN_HEADERS
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.cellTestTag
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.headersTestTag
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.rowHeaderTestTag

internal fun dataSetTableRobot(
    composeTestRule: ComposeContentTestRule,
    dataSetTableRobot: DataSetTableRobot.() -> Unit
) {
    DataSetTableRobot(composeTestRule).apply {
        dataSetTableRobot()
    }
}

internal class DataSetTableRobot(
    private val composeTestRule: ComposeContentTestRule
) : BaseRobot() {

    fun clickOnSaveButton() {
        waitForView(withId(R.id.saveButton)).perform(click())
    }

    fun clickOnNegativeButton() {
        onView(withId(R.id.negative)).perform(click())
    }

    fun assertItemWithTextIsDisplayed(text: String, substring: Boolean) {
        itemWithTextIsDisplayed(text, substring, composeTestRule)
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

    private fun scrollToItemWithTag(tag: String) {
        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN").performScrollToNode(hasTestTag(tag))
    }

    fun scrollToItemWithText(text: String) {
        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN").performScrollToNode(hasText(text, substring = true))
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

    fun syncIsAvailable() {
        composeTestRule.onNodeWithTag(SYNC_BUTTON_TAG)
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText("Refresh")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Not now")
            .assertIsDisplayed()
            .performClick()
    }

    fun indicatorTableIsDisplayed() {
        assertTableIsDisplayed()

        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .performScrollToIndex(15)
        composeTestRule.onNodeWithText("Moderate malnutrition rate", useUnmergedTree = true)
            .assertIsDisplayed()

    }

    fun totalsAreDisplayed(
        tableId: String,
        totalColumnHeaderRowIndex: Int,
        totalColumnHeaderColumnIndex: Int,
    ) {
        composeTestRule.onNodeWithTag("HEADER_CELL$tableId$totalColumnHeaderRowIndex$totalColumnHeaderColumnIndex")
            .performScrollTo()

        composeTestRule.onNodeWithTag(headersTestTag(tableId))
            .assertIsDisplayed()
            .performTouchInput {
                swipeRight()
                swipeRight()
            }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(rowHeaderTestTag(tableId, "${tableId}_totals"), true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .performScrollToIndex(0)
    }

    fun assertInputDialogIsDisplayed() {
        composeTestRule.onNodeWithTag(INPUT_DIALOG_TAG).assertIsDisplayed()
    }

    fun assertInputDescriptionIsDisplayed(description: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(description, useUnmergedTree = true).assertIsDisplayed()
    }

    fun typeOnInputDialog(value: String, inputTestTag: String) {
        composeTestRule.onNodeWithTag(inputTestTag).performTextInput(value)
        composeTestRule.onNodeWithTag(INPUT_DIALOG_DONE_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertCellHasValue(
        tableId: String,
        cellId: String,
        expectedValue: String
    ) {
        assertTableIsDisplayed()
        composeTestRule.waitForIdle()
        scrollToItemWithTag(cellTestTag(tableId, cellId))
        composeTestRule.onNodeWithTag(cellTestTag(tableId, cellId), true)
            .onChildren()
            .filter(hasTestTag("CELL_VALUE_TEST_TAG"))
            .assertAny(hasText(expectedValue))
    }

    fun assertRowTotalValue(
        tableId: String,
        rowIndex: Int,
        expectedValue: String,
    ) {
        composeTestRule.onNodeWithTag("HEADER_CELL${tableId}12")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNode(
                hasParent(hasTestTag("CELL_TEST_TAG_${tableId}${tableId}_${rowIndex}_totals")) and
                        hasTestTag("CELL_VALUE_TEST_TAG") and
                        hasText(expectedValue),
                true
            )
            .assertIsDisplayed()

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

        composeTestRule.onNodeWithTag(
            "CELL_TEST_TAG_${tableId}${tableId}_totals_$columnIndex",
            true
        )
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

    fun tapOnSaveButton() {
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).performClick()

    }

    fun checkCompleteDialogIsDisplayed() {
        composeTestRule.onNodeWithTag("COMPLETION").assertIsDisplayed()
    }

    fun tapOnCompleteButton() {
        composeTestRule.onNodeWithTag(COMPLETION_DIALOG_BUTTON_TEST_TAG).performClick()
    }

    fun checkMandatoryDialogIsDisplayed() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("MANDATORY_FIELDS").assertIsDisplayed()
    }

    fun acceptMandatoryDialog() {
        composeTestRule.onNodeWithTag(MANDATORY_FIELDS_DIALOG_OK_BUTTON_TEST_TAG).performClick()
    }

    fun assertValidationBarIsDisplayed() {
        composeTestRule.onNodeWithTag(VALIDATION_BAR_TEST_TAG).assertIsDisplayed()
    }

    fun expandValidationRulesErrorDialog() {
        composeTestRule.onNodeWithTag(VALIDATION_BAR_EXPAND_TEST_TAG).performClick()
    }

    fun tapOnReview() {
        composeTestRule.onNodeWithTag(VALIDATION_DIALOG_REVIEW_BUTTON_TEST_TAG).performClick()
    }

    fun acceptOptionalValidationRule() {
        composeTestRule.onNodeWithTag(OPTIONAL_VALIDATION_RULE_DIALOG_ACCEPT_TEST_TAG)
            .performClick()
    }

    fun tapOnCompleteAnyway() {
        composeTestRule.onNodeWithTag(VALIDATION_DIALOG_COMPLETE_ANYWAY_BUTTON_TEST_TAG)
            .performClick()
    }

    fun assertTablesAreDisplayedInOrder(tableIds: List<String>) {
        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .onChildren()
            .fetchSemanticsNodes()
            .filter {
                it.config.getOrElse(TestTag) { "" }.startsWith(TEST_TAG_COLUMN_HEADERS)
            }.forEachIndexed { index, semanticsNode ->
                semanticsNode.config.getOrElse(TestTag) { "" }.endsWith(tableIds[index])
            }
    }

    fun assertCategoryAsRowsAreDisplayed(
        dataElementsRowTestTags: List<CellData>,
        rowTestTags: List<CellData>
    ) {
        dataElementsRowTestTags.forEach { deCellData ->
            val dataElementIsDisplayed = composeTestRule.onNode(
                hasTestTag(deCellData.testTag) and hasTextExactly(deCellData.label)
            ).performScrollTo()
                .assertIsDisplayed()
            rowTestTags.forEach { catCellData ->
                dataElementIsDisplayed.assert(
                    hasAnySibling(hasTestTag(catCellData.testTag) and hasTextExactly(catCellData.label))
                )
            }
        }
    }

    fun assertCategoryHeaderIsNotDisplayed(headerTestTags: List<CellData>) {
        headerTestTags.forEach { cellData ->
            composeTestRule.onNode(
                hasTestTag(cellData.testTag) and
                        hasTextExactly(cellData.label),
            ).assertDoesNotExist()
        }
    }

    fun assertCategoryHeaderIsDisplayed(headerTestTags: List<CellData>) {
        headerTestTags.forEach { cellData ->
            composeTestRule.onNode(
                hasTestTag(cellData.testTag) and
                        hasText(cellData.label)
            ).assertExists()
        }
    }

    fun clickOnSection(sectionIndex: Int, sectionName: String) {
        composeTestRule.onNode(
            hasTestTag("SCROLLABLE_TAB_$sectionIndex") and
                    hasText(sectionName)
        )
            .performScrollTo()
            .performClick()
    }

    fun assertTableHeaders(headerTestTags: List<CellData>) {
        headerTestTags.forEach { cellData ->
            scrollToItemWithTag(cellData.testTag)
            composeTestRule.onNode(
                hasTestTag(cellData.testTag) and
                        hasText(cellData.label)
            ).assertExists()

        }
    }

    fun assertTableRows(rowTestTags: List<CellData>) {
        rowTestTags.forEach { cellData ->
            scrollToItemWithTag(cellData.testTag)
            composeTestRule.onNode(
                hasTestTag(cellData.testTag) and
                        hasTextExactly(cellData.label)
            ).assertExists()
        }
    }
}
