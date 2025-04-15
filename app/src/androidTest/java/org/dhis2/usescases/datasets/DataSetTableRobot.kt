package org.dhis2.usescases.datasets

import androidx.compose.ui.semantics.SemanticsProperties.TestTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.ComposeContentTestRule
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
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.composetable.ui.semantics.CELL_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.COMPLETION_DIALOG_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_DISMISS_TAG
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_NEXT_TAG
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_TAG
import org.dhis2.mobile.aggregates.ui.constants.MANDATORY_FIELDS_DIALOG_OK_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.OPTIONAL_VALIDATION_RULE_DIALOG_ACCEPT_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.SAVE_BUTTON_TAG
import org.dhis2.mobile.aggregates.ui.constants.SYNC_BUTTON_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_BAR_EXPAND_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_BAR_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_DIALOG_COMPLETE_ANYWAY_BUTTON_TEST_TAG
import org.dhis2.mobile.aggregates.ui.constants.VALIDATION_DIALOG_REVIEW_BUTTON_TEST_TAG
import org.dhis2.ui.toColor
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.RowBackground
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

    fun clickOnCell(tableId: String, cellId: String) {
        scrollToItemWithTag(cellTestTag(tableId, cellId))
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
        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .performScrollToNode(hasTestTag(tag))
    }

    fun scrollToItemWithText(text: String) {
        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN")
            .performScrollToNode(hasText(text, substring = true))
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
        composeTestRule.onNodeWithTag(headersTestTag(tableId)).printToLog("HEADERSSS")

        composeTestRule.onNodeWithTag(headersTestTag(tableId)).assertIsDisplayed()
            .performScrollToNode(
                hasAnyChild(hasTestTag("HEADER_CELL$tableId$totalColumnHeaderRowIndex$totalColumnHeaderColumnIndex"))
            )

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
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(INPUT_DIALOG_TAG).assertIsDisplayed()
    }

    fun assertInputDescriptionIsDisplayed(description: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(description, useUnmergedTree = true).assertIsDisplayed()
    }

    fun typeOnInputDialog(value: String, inputTestTag: String) {
        composeTestRule.onNodeWithTag(inputTestTag).performTextReplacement(value)
    }

    fun pressOnInputDialogNext() {
        composeTestRule.onNodeWithTag(INPUT_DIALOG_NEXT_TAG).performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun pressOnInputDialogDismiss() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(INPUT_DIALOG_DISMISS_TAG) and isEnabled(),
            timeoutMillis = TIMEOUT
        )

        composeTestRule.onNodeWithTag(INPUT_DIALOG_DISMISS_TAG).performClick()

        composeTestRule.waitUntilDoesNotExist(
            hasTestTag(INPUT_DIALOG_TAG),
            timeoutMillis = TIMEOUT
        )
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

    @OptIn(ExperimentalTestApi::class)
    fun tapOnSaveButton() {
        composeTestRule.waitUntilExactlyOneExists(
            matcher = hasTestTag(SAVE_BUTTON_TAG),
            timeoutMillis = TIMEOUT
        )
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkCompleteDialogIsDisplayed() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag("COMPLETION"),
            timeoutMillis = TIMEOUT
        )
        composeTestRule.onNodeWithTag("COMPLETION").assertIsDisplayed()
    }

    fun tapOnCompleteButton() {
        composeTestRule.onNodeWithTag(COMPLETION_DIALOG_BUTTON_TEST_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkMandatoryDialogIsDisplayed() {
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag("MANDATORY_FIELDS"),
            3000
        )
        composeTestRule.onNodeWithTag("MANDATORY_FIELDS").assertIsDisplayed()
    }

    fun acceptMandatoryDialog() {
        composeTestRule.onNodeWithTag(MANDATORY_FIELDS_DIALOG_OK_BUTTON_TEST_TAG).performClick()
        composeTestRule.waitForIdle()
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
                hasTestTag(deCellData.testTag) and hasText(deCellData.label)
            ).performScrollTo()
                .assertIsDisplayed()
            rowTestTags.forEach { catCellData ->
                dataElementIsDisplayed.assert(
                    hasAnySibling(hasTestTag(catCellData.testTag) and hasText(catCellData.label))
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

    @OptIn(ExperimentalTestApi::class)
    fun assertCellBackgroundColor(
        tableId: String,
        cellId: String,
        expectedValue: String,
        expectedColor: String
    ) {
        val expectedAlphaColor = expectedColor.toColor().copy(0.3f)

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(cellTestTag(tableId, cellId)) and hasText(expectedValue),
            timeoutMillis = 3000
        )

        composeTestRule.onNodeWithTag(cellTestTag(tableId, cellId))
            .assertIsDisplayed()
            .assert(hasText(expectedValue))
            .assert(SemanticsMatcher.expectValue(RowBackground, expectedAlphaColor))
    }

    fun assertInputLegendDescription(expectedLabel: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(expectedLabel).assertIsDisplayed()
    }
}
