package org.dhis2.usescases.datasets

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.dataSetTable.period.reportPeriodSelectorRobot
import org.dhis2.usescases.datasets.dataSetTable.pivotTestingData
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
import org.dhis2.usescases.searchte.robot.filterRobot
import org.hisp.dhis.android.core.D2Manager
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class DataSetTest : BaseTest() {

    @get:Rule
    val ruleDataSetDetail = lazyActivityScenarioRule<DataSetDetailActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    override fun teardown() {
        super.teardown()
        cleanLocalDatabase()
    }

    @Test
    fun datasetAutomate() = runTest {
        val orgUnit = "Ngelehun CHC"

        enterDataSetStep(
            uid = "BfMAe6Itzgt",
            name = "Child Health",
        )
        dataSetInstanceInChronologicalOrderStep()
        createDataSetInstanceStep(
            orgUnit = orgUnit,
            openFuturePeriods = 9,
        )

        tableIsVisible()
        checkImmunizationTableIsDisplayed()
        syncButtonIsAvailableStep()
        checkIndicatorsStep()
        checkTotals()
        enterDataStep(
            tableId = "dzjKKQq0cSO",
            cellId = "PGRlPnM0Nm01TVMwaHh1Ojxjb2M+UHJsdDBDMVJGMHM=",
            dataElementDescription = "BCG doses administered.",
            value = "12",
            inputTestTag = "INPUT_INTEGER_FIELD"
        )
        checkTotalsUpdated(
            tableId = "dzjKKQq0cSO",
            rowIndex = 0,
            value = "12.0",
        )
        checkGreyFields()
        reenterDataSetToCheckValueSavedStep()

        checkFilterCombination(orgUnit)
    }

    @Test
    fun formConfigurationTestAutomate() = runTest {
        enterDataSetStep("DMicXfEri6s", "Form configuration options")
        waitForTableToBeVisible()

        // Step - ANDROAPP-6858 Custom title is displayed
        checkCustomTitleIsDisplayed()
        // Step - ANDROAPP-6795 Check content boxes above and below the table
        checkContentBoxesAreDisplayed()
        // Step - ANDROAPP-6810 Move a category to rows (click on sections 8, 16, 24)
        checkCategoryIsMovedToRow()
        // Step - ANDROAPP-6828 Automatic grouping (click on sections 19, 20, 22)
        checkAutomaticGroupingDisabled()
        // Step - ANDROAPP-6811 Pivot options (click on sections 5, 13, 23)
        checkPivotOptions()
    }

    @Test
    fun checkAllSections() = runTest {
        enterDataSetStep("DMicXfEri6s", "Form configuration options")
        waitForTableToBeVisible()
        D2Manager.getD2().dataSetModule().sections().byDataSetUid().eq("DMicXfEri6s").blockingGet()
            .forEachIndexed { sectionIndex, section ->
                dataSetTableRobot(composeTestRule) {
                    clickOnSection(sectionIndex, section.displayName()!!)
                    assertTableIsDisplayed()
                }
            }
    }




    @Test
    fun saveAndCompleteMandatoryFieldMandatoryValidationRule() = runTest {
        val dataSetUid = "Lpw6GcnTrmS"
        val dataSetName = "Emergency Response"
        val orgUnit = "Ngelehun CHC"
        val catCombo = "Result"
        val tableId = "bjDvmb4bfuf"
        val cellValidationRuleId = "PGRlPktGbkZwYnFEcWppOjxjb2M+SGxsdlg1MGNYQzA="
        val cellMandatoryId = "PGRlPnpGRmIzYmFyNEN0Ojxjb2M+SGxsdlg1MGNYQzA="

        enterDataSetStep(
            uid = dataSetUid,
            name = dataSetName,
        )

        createDataSetInstanceStep(
            orgUnit = orgUnit,
            catCombo = catCombo,
        )

        tapOnSaveButtonStep()

        checkValidationBarIsDisplayedAndReview()

        enterDataStep(
            tableId = tableId,
            cellId = cellValidationRuleId,
            value = "1",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        tapOnSaveButtonStep()

        checkCompleteDialogIsDisplayedAndAttemptToCompleteStep()

        checkMandatoryDialogIsDisplayedAndAcceptStep()

        checkMandatoryIconOnCell(tableId = tableId, cellId = cellMandatoryId)

        enterDataStep(
            tableId = tableId,
            cellId = cellMandatoryId,
            value = "2",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        tapOnSaveButtonStep()

        checkCompleteDialogIsDisplayedAndAttemptToCompleteStep()

        checkDataSetInstanceHasBeenCreatedAndIsCompleted(orgUnit)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testPeriodScrollBehaviorWithFuturePeriods() = runTest {
        val dataSetUid = "BfMAe6Itzgt"
        val dataSetName = "Child Health"
        val orgUnit = "Ngelehun CHC"
        val openFuturePeriods = 10

        enterDataSetStep(
            uid = dataSetUid,
            name = dataSetName,
        )

        dataSetDetailRobot(composeTestRule) {
            clickOnAddDataSet()
        }

        dataSetInitialRobot(composeTestRule) {
            clickOnInputOrgUnit()
        }

        orgUnitSelectorRobot(composeTestRule) {
            selectTreeOrgUnit(orgUnit)
        }

        // open the period input
        dataSetInitialRobot(composeTestRule) {
            clickOnInputPeriod()
        }

        reportPeriodSelectorRobot(composeTestRule) {

            val today = LocalDate.now()
            val latest = today.plusMonths(openFuturePeriods - 1L)

            val df = DateTimeFormatter.ofPattern("MMMM yyyy")

            // wait until the period selector is on the screen
            composeTestRule.waitUntil {
                composeTestRule
                    .onNodeWithTag("period_selector")
                    .isDisplayed()
            }

            // assert the item corresponding to "today" is the first item being displayed
            composeTestRule
                .onNodeWithTag("period_item_${openFuturePeriods - 1}")
                .assertIsDisplayed()
                .assertTextEquals(today.format(df))

            // assert the next period after "today", which is in the future, is not being displayed
            composeTestRule
                .onNodeWithTag("period_item_${openFuturePeriods - 2}")
                .assertIsNotDisplayed()

            // scroll the the latest future date in the selector and check it is the month/year
            // we expect based on the number of future periods

            composeTestRule
                .onNodeWithTag("period_selector")
                .performScrollToNode(hasTestTag("period_item_0"))

            composeTestRule
                .onNodeWithTag("period_item_0")
                .assertIsDisplayed()
                .assertTextEquals(df.format(latest))
        }
    }

    @Test
    fun saveAndCompleteOptionalValidationRule() = runTest {
        val dataSetUid = "Nyh6laLdBEJ"
        val dataSetName = "IDSR weekly"
        val orgUnit = "Ngelehun CHC"
        val tableId = "gbvX3pogf7p"
        val cellMandatoryFieldCombination01Id = "PGRlPkJveTNRd3p0Z2VaOjxjb2M+SjJRZjFqdFp1ajg="
        val cellMandatoryFieldCombination02Id = "PGRlPkJveTNRd3p0Z2VaOjxjb2M+clFMRm5OWFhJTDA="
        val legendTableId = "bjDvmb4bfuf"
        val cellLegendId = "PGRlPlVzU1VYMGNwS3NIOjxjb2M+SGxsdlg1MGNYQzA="

        enterDataSetStep(
            uid = dataSetUid,
            name = dataSetName,
        )

        createDataSetInstanceStep(
            orgUnit = orgUnit,
        )

        checkLegendsStep(
            tableId = legendTableId,
            cellId = cellLegendId,
            legendData = legendTestingData
        )

        enterDataStep(
            tableId = tableId,
            cellId = cellMandatoryFieldCombination01Id,
            value = "1",
            inputTestTag = "INPUT_POSITIVE_INTEGER_OR_ZERO_FIELD"
        )

        tapOnSaveButtonStep()

        runOptionalValidationRules()

        checkValidationBarIsDisplayedAndCompleteAnyway()

        checkMandatoryDialogIsDisplayedAndAcceptStep()

        enterTwoSequentialSteps(
            tableId = tableId,
            firstCellId = cellMandatoryFieldCombination02Id,
            firstValue = "2",
            secondValue = "3",
            inputTestTag = "INPUT_POSITIVE_INTEGER_OR_ZERO_FIELD"
        )

        tapOnSaveButtonStep()

        runOptionalValidationRules()

        checkValidationBarIsDisplayedAndCompleteAnyway()
        checkDataSetInstanceHasBeenCreatedAndIsCompleted(orgUnit)

    }

    @Test
    fun completeExpiryAndFutureDays() = runTest {
        val dataSetUid = "TuL8IOPzpHh"
        val dataSetName = "EPI Stock"
        val catCombo = "Improve access to clean water"
        val orgUnit = "Ngelehun CHC"
        val formatter = DateTimeFormatter.ofPattern("MMddyyyy")
        val today = LocalDate.now()
        val threeDaysFromNow = today.plusDays(3)
        val fiveDaysAgo = today.minusDays(5)
        val tableId = "bjDvmb4bfuf"
        val cellId = "PGRlPlhOcmpYcVpySEQ4Ojxjb2M+SGxsdlg1MGNYQzA="
        val threeDaysFromNowStr = threeDaysFromNow.format(formatter)
        val fiveDaysAgoStr = fiveDaysAgo.format(formatter)
        enterDataSetStep(
            uid = dataSetUid,
            name = dataSetName,
        )
        createDailyPeriodDataSetInstanceStep(
            date = fiveDaysAgoStr,
            orgUnit = orgUnit,
            catCombo = catCombo
        )

        checkTableIsNotEditable()
        dataSetTableRobot(composeTestRule) {
            tapOnSaveButton()
        }
        composeTestRule.waitForIdle()
        createDailyPeriodDataSetInstanceStep(
            date = threeDaysFromNowStr,
            orgUnit = orgUnit,
            catCombo = catCombo
        )
        // Wait for table to be ready after creating the second dataset instance
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag("TABLE_SCROLLABLE_COLUMN"),
            timeoutMillis = 10000
        )
        tableIsVisible()
        enterDataStep(
            tableId = tableId,
            cellId = cellId,
            value = "10",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )
        tapOnSaveButtonStep()
        dataSetTableRobot(composeTestRule) {
            checkCompleteDialogIsDisplayed()
            tapOnCompleteButton()
        }

        dataSetDetailRobot(composeTestRule){
            clickOnDataSetAtPosition(0)
        }
        tableIsVisible()
        dataSetTableRobot(composeTestRule) {
            checkItemWithTextIsDisplayed("Re-open form to edit")
            tapOnReopenButton()
            checkItemWithTextIsNotDisplayed("Re-open form to edit")
            tapOnSaveButton()
            tapOnNotNowButton()
        }

        dataSetDetailRobot(composeTestRule){
            checkDataSetIsNotCompletedAndModified(catCombo, orgUnit)
        }


    }

    private fun checkCustomTitleIsDisplayed() {
        dataSetDetailRobot(composeTestRule) {
            assertItemWithTextIsDisplayed("Line end: Custom Title", true)
            assertItemWithTextIsDisplayed(
                "Line end: Custom Subtitle test a very long subtitle",
                true
            )
        }
    }

    private suspend fun waitForTableToBeVisible() {
        composeTestRule.awaitIdle()
        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }
        tableIsVisible()
    }

    private suspend fun checkTableIsNotEditable() {
        tableIsVisible()
        composeTestRule.onNodeWithTag("TABLE_SCROLLABLE_COLUMN").printToLog("TABLE_LOG")
        dataSetTableRobot(composeTestRule) {
            checkItemWithTextIsDisplayed("This data is not editable")
        }
        composeTestRule.waitForIdle()
    }

    private suspend fun checkContentBoxesAreDisplayed() {
        tableIsVisible()
        // Check top and bottom content is displayed in initial section
        dataSetDetailRobot(composeTestRule) {
            assertItemWithTextIsDisplayed("CONTENT BEFORE 1:", true)
        }
        dataSetTableRobot(composeTestRule) {
            scrollToItemWithText("CONTENT AFTER 1:")
            assertItemWithTextIsDisplayed("CONTENT AFTER 1:", true)
        }
        // Check top and bottom content is displayed when changing sections
        dataSetDetailRobot(composeTestRule) {
            clickOnSection("SCROLLABLE_TAB_1")
        }
        tableIsVisible()
        // Check top and bottom content is displayed when changing sections
        dataSetDetailRobot(composeTestRule) {
            assertItemWithTextIsDisplayed("CONTENT BEFORE 2:", true)
        }
        dataSetTableRobot(composeTestRule) {
            scrollToItemWithText("CONTENT AFTER 2:")
            assertItemWithTextIsDisplayed("CONTENT AFTER 2:", true)
            scrollToTop()
        }
    }

    private fun checkLegendsStep(
        tableId: String,
        cellId: String,
        legendData: List<LegendTestingData>
    ) {
        dataSetTableRobot(composeTestRule) {
            clickOnCell(tableId, cellId)
            assertInputDialogIsDisplayed()
            legendData.forEach { data ->
                typeOnInputDialog(
                    value = data.valueToType,
                    inputTestTag = "INPUT_POSITIVE_INTEGER_OR_ZERO_FIELD",
                )
                closeKeyboard()
                composeTestRule.waitForIdle()
                assertCellBackgroundColor(
                    tableId,
                    cellId,
                    data.valueToType,
                    data.expectedColor
                )
                assertInputLegendDescription(data.expectedLabel)
            }
            typeOnInputDialog(
                value = "",
                inputTestTag = "INPUT_POSITIVE_INTEGER_OR_ZERO_FIELD",
            )
            pressOnInputDialogDismiss()
            closeKeyboard()
        }
    }

    private suspend fun checkCategoryIsMovedToRow() {
        val cellIdSection8 = "PGRlPlAzakpINVR1NVZDLCA8Y28+RmJMWlMzdWVXYlE6PGNvPg=="
        val cellId2Section8 = "PGRlPkZRMm84VUJsY3JTLCA8Y28+RmJMWlMzdWVXYlE6PGNvPg=="
        val cellIdSection16 = "PGRlPkFyUzdWeXVMOTVmLCA8Y28+RmJMWlMzdWVXYlE6PGNvPg=="
        val cellId2Section16 = "PGRlPkFyUzdWeXVMOTVmLCA8Y28+Wlp4WXVvVENjRGQ6PGNvPg=="
        val cellIdSection24 = "PGRlPnpnZUFkbnBTWTVLLCA8Y28+WjhhWDNBa3JETVM6PGNvPg=="
        val cellId2Section24 = "PGRlPnpnZUFkbnBTWTVLLCA8Y28+c05yMXk1UXExWVE6PGNvPg=="

        dataSetTableRobot(composeTestRule) {
            clickOnSection(categoryToRowList[0].sectionIndex, categoryToRowList[0].sectionName)
        }
        tableIsVisible()
        enterDataStep(
            tableId = "t3aNCvHsoSn",
            cellId = cellIdSection8,
            value = "10",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )
        enterDataStep(
            tableId = "t3aNCvHsoSn",
            cellId = cellId2Section8,
            value = "7",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        dataSetTableRobot(composeTestRule) {
            clickOnSection(categoryToRowList[1].sectionIndex, categoryToRowList[1].sectionName)
        }
        tableIsVisible()
        dataSetTableRobot(composeTestRule) {
            scrollToTop()
        }
        enterDataStep(
            tableId = "t3aNCvHsoSn",
            cellId = cellIdSection16,
            value = "11",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )
        enterDataStep(
            tableId = "t3aNCvHsoSn",
            cellId = cellId2Section16,
            value = "24",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        dataSetTableRobot(composeTestRule) {
            clickOnSection(categoryToRowList[2].sectionIndex, categoryToRowList[2].sectionName)
        }
        tableIsVisible()

        enterDataStep(
            tableId = "aN8uN5b15YG_1",
            cellId = cellIdSection24,
            value = "4",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )
        enterDataStep(
            tableId = "aN8uN5b15YG_1",
            cellId = cellId2Section24,
            value = "14",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        dataSetTableRobot(composeTestRule) {
            categoryToRowList.forEach { data ->
                clickOnSection(data.sectionIndex, data.sectionName)
                assertTableIsDisplayed()
                assertCategoryAsRowsAreDisplayed(data.dataElementsRowTestTags, data.rowTestTags)
                assertCategoryHeaderIsNotDisplayed(data.pivotedHeaderTestTags)
                assertCategoryHeaderIsDisplayed(data.headerTestTags)
            }
        }
    }

    private fun runOptionalValidationRules() {
        dataSetTableRobot(composeTestRule) {
            acceptOptionalValidationRule()
        }
    }

    private fun checkValidationBarIsDisplayedAndReview() {
        dataSetTableRobot(composeTestRule) {
            assertValidationBarIsDisplayed()
            expandValidationRulesErrorDialog()
            tapOnReview()
        }
    }

    private fun checkValidationBarIsDisplayedAndCompleteAnyway() {
        dataSetTableRobot(composeTestRule) {
            assertValidationBarIsDisplayed()
            expandValidationRulesErrorDialog()
            tapOnCompleteAnyway()
        }
    }

    private fun checkDataSetInstanceHasBeenCreatedAndIsCompleted(
        orgUnit: String,
    ) {
        dataSetDetailRobot(composeTestRule) {
            checkDataSetIsCompletedAndModified(orgUnit)
        }
    }

    private fun checkMandatoryDialogIsDisplayedAndAcceptStep() {
        dataSetTableRobot(composeTestRule) {
            checkMandatoryDialogIsDisplayed()
            acceptMandatoryDialog()
        }
    }

    private suspend fun checkAutomaticGroupingDisabled() {
        val table19 = "t3aNCvHsoSn_0"
        val table219 = "aN8uN5b15YG_1"
        val table20 = "ck7mRNwGDjP_1"
        val table220 = "ck7mRNwGDjP_3"
        val table22 = "t3aNCvHsoSn_0"
        val cellIdSection19 = "PGNvYz5TMzRVTE1jSE1jYTo8ZGU+Q2o1clRjOW5Fdmw="
        val cellId2Section19 = "PGNvYz5ET0M3ZW1MenlSaTo8ZGU+RXpSNVkyVjBKRjk="
        val cellIdSection20 = "PGRlPnlxQmtuOUNXS2loOjxjb2M+bzJneEV0NkVrMkM="
        val cellId2Section20 = "PGRlPmxhWkxRZG51Y1YxOjxjb2M+bzJneEV0NkVrMkM="
        val cellIdSection22 = "PGRlPndjd2JOMWpSMGFyOjxjb2M+U2RPVUkyeVQ0Nkg="
        dataSetTableRobot(composeTestRule) {
            clickOnSection(
                disableAutomaticGroupingList[0].sectionIndex,
                disableAutomaticGroupingList[0].sectionName
            )
        }
        tableIsVisible()
        enterDataStep(
            tableId = table19,
            cellId = cellIdSection19,
            value = "4",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )
        enterDataStep(
            tableId = table219,
            cellId = cellId2Section19,
            value = "14",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        dataSetTableRobot(composeTestRule) {
            clickOnSection(
                disableAutomaticGroupingList[1].sectionIndex,
                disableAutomaticGroupingList[1].sectionName
            )
        }
        tableIsVisible()

        enterDataStep(
            tableId = table20,
            cellId = cellIdSection20,
            value = "7",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )
        enterDataStep(
            tableId = table220,
            cellId = cellId2Section20,
            value = "11",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        dataSetTableRobot(composeTestRule) {
            clickOnSection(
                disableAutomaticGroupingList[2].sectionIndex,
                disableAutomaticGroupingList[2].sectionName
            )
        }
        tableIsVisible()

        enterDataStep(
            tableId = table22,
            cellId = cellIdSection22,
            value = "7",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )
        composeTestRule.waitForIdle()

        dataSetTableRobot(composeTestRule) {
            disableAutomaticGroupingList.forEach { data ->
                clickOnSection(data.sectionIndex, data.sectionName)
                assertTableIsDisplayed()
                assertTablesAreDisplayedInOrder(data.tableIdTestTags)
            }
        }
    }

    private suspend fun checkPivotOptions() {
        val table5 = "aN8uN5b15YG"
        val table23 = "aN8uN5b15YG_1"
        val cellIdSection5 = "PGNvYz5ET0M3ZW1MenlSaTo8ZGU+TFNKNW1LcHlFdjE="
        val cellIdSection23 = "PGNvYz5SMjNoOVFaUmJSdDo8ZGU+ZElxeDdyZG5WYzk="
        dataSetTableRobot(composeTestRule) {
            clickOnSection(pivotTestingData[0].sectionIndex, pivotTestingData[0].sectionName)
        }
        tableIsVisible()
        enterDataStep(
            tableId = table5,
            cellId = cellIdSection5,
            value = "4",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        dataSetTableRobot(composeTestRule) {
            clickOnSection(pivotTestingData[2].sectionIndex, pivotTestingData[2].sectionName)
        }
        tableIsVisible()
        enterDataStep(
            tableId = table23,
            cellId = cellIdSection23,
            value = "15",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        dataSetTableRobot(composeTestRule) {
            pivotTestingData.forEach { data ->
                scrollToTop()
                clickOnSection(data.sectionIndex, data.sectionName)
                assertTableIsDisplayed()
                assertTableHeaders(data.headerTestTags)
                assertTableRows(data.rowTestTags)
            }
        }
    }

    private fun checkCompleteDialogIsDisplayedAndAttemptToCompleteStep() {
        dataSetTableRobot(composeTestRule) {
            checkCompleteDialogIsDisplayed()
            tapOnCompleteButton()
        }
    }

    private fun tapOnSaveButtonStep() {
        dataSetTableRobot(composeTestRule) {
            tapOnSaveButton()
        }
    }

    private fun enterDataSetStep(
        uid: String,
        name: String,
    ) {
        startDataSetDetailActivity(
            dataSetUid = uid,
            dataSetName = name,
            rule = ruleDataSetDetail
        )
    }

    private fun dataSetInstanceInChronologicalOrderStep() {
        dataSetDetailRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            checkDatasetListIsSortedChronologically()
        }
    }

    private suspend fun tableIsVisible() {
        composeTestRule.awaitIdle()
        dataSetTableRobot(composeTestRule) {
            assertTableIsDisplayed()
        }
    }

    private suspend fun checkImmunizationTableIsDisplayed() {
        composeTestRule.awaitIdle()
        dataSetTableRobot(composeTestRule) {
            assertImmunizationTableIsDisplayed()
        }
    }

    private suspend fun syncButtonIsAvailableStep() {
        composeTestRule.awaitIdle()
        dataSetTableRobot(composeTestRule) {
            syncIsAvailable()
        }
    }

    private suspend fun checkTotals() {
        composeTestRule.awaitIdle()
        dataSetTableRobot(composeTestRule) {
            totalsAreDisplayed(
                tableId = "dzjKKQq0cSO",
                totalColumnHeaderRowIndex = 1,
                totalColumnHeaderColumnIndex = 4,
            )
        }
    }

    /**
     * Enters data in a cell and checks if the value is saved
     * @param tableId The table id
     * @param cellId The cell id
     * @param dataElementDescription The data element description (optional)
     * @param value The value to enter
     * @param inputTestTag The input test tag to identify the input field base on the ValueType
     */
    private fun enterDataStep(
        tableId: String,
        cellId: String,
        dataElementDescription: String? = null,
        value: String,
        inputTestTag: String,
    ) {
        dataSetTableRobot(composeTestRule) {
            clickOnCell(tableId, cellId)
            assertInputDialogIsDisplayed()
            dataElementDescription?.let {
                assertInputDescriptionIsDisplayed(dataElementDescription)
            }
            typeOnInputDialog(value, inputTestTag)
            pressOnInputDialogDismiss()
            assertCellHasValue(tableId, cellId, value)
        }

    }

    private fun enterTwoSequentialSteps(
        tableId: String,
        firstCellId: String,
        firstValue: String,
        secondValue: String,
        inputTestTag: String,

        ) {
        dataSetTableRobot(composeTestRule) {
            clickOnCell(tableId, firstCellId)
            typeOnInputDialog(firstValue, inputTestTag)
            pressOnInputDialogNext()
            typeOnInputDialog(secondValue, inputTestTag)
            pressOnInputDialogDismiss()
        }
    }

    private fun checkTotalsUpdated(
        tableId: String,
        rowIndex: Int,
        value: String
    ) {
        dataSetTableRobot(composeTestRule) {
            assertRowTotalValue(tableId, rowIndex, value)
        }
    }

    private suspend fun checkIndicatorsStep() {
        composeTestRule.awaitIdle()
        dataSetTableRobot(composeTestRule) {
            indicatorTableIsDisplayed()
        }
    }

    private suspend fun reenterDataSetToCheckValueSavedStep() {
        val cell00Id = "PGRlPnM0Nm01TVMwaHh1Ojxjb2M+UHJsdDBDMVJGMHM="

        dataSetTableRobot(composeTestRule) {
            returnToDataSetInstanceList()
        }
        dataSetDetailRobot(composeTestRule) {
            clickOnDataSetAtPosition(0)
        }
        checkImmunizationTableIsDisplayed()

        dataSetTableRobot(composeTestRule) {
            assertCellHasValue("dzjKKQq0cSO", cell00Id, "12")
        }
    }

    private fun checkFilterCombination(
        orgUnit: String,
    ) {

        dataSetTableRobot(composeTestRule) {
            returnToDataSetInstanceList()
        }

        dataSetDetailRobot(composeTestRule) {
            assertEquals(6, getListItemCount())
            filterRobot(composeTestRule) {
                //Open filter
                openFilters()

                clickOnFilterBy(filter = "ORG. UNIT")
                typeOrgUnitField(orgUnit)
                checkFilterCounter("1")
            }
            assertEquals(5, getListItemCount())
        }

        filterRobot(composeTestRule) {
            //Filter by period
            clickOnFilterBy(filter = "Period")
            clickOnFromToDate()
            chooseDate("08082024")
            chooseDate("09092026")
            checkFilterCounter("2")
        }

        dataSetDetailRobot(composeTestRule) {
            checkDataSetRecyclerItemsAreDisplayed(2)
        }

        filterRobot(composeTestRule) {
            clickOnFilterBy(filter = "Sync")
            clickOnNotSync()
            checkFilterCounter("3")
        }

        dataSetDetailRobot(composeTestRule) {
            assertEquals(1, getListItemCount())
        }

        filterRobot(composeTestRule) {
            resetFilters()
        }
    }

    private fun checkGreyFields() {
        dataSetTableRobot(composeTestRule) {
            val tableId = "dzjKKQq0cSO"
            val firstCell = "PGRlPnBpa096aXlDWGJNOjxjb2M+UHJsdDBDMVJGMHM="
            val greyCell1 = "PGRlPnBpa096aXlDWGJNOjxjb2M+cHNid3AzQ1FFaHM="
            val nextCell = "PGRlPnBpa096aXlDWGJNOjxjb2M+VjZMNDI1cFQzQTA="
            val greyCell2 = "PGRlPnBpa096aXlDWGJNOjxjb2M+aEVGS1NzUFY1ZXQ="
            val nextLineCell = "PGRlPk8wNW1BQnlPZ0F2Ojxjb2M+UHJsdDBDMVJGMHM="
            clickOnCell(tableId, firstCell)
            assertInputDialogIsDisplayed()
            pressOnInputDialogNext()
            assertCellDisabled(tableId, greyCell1)
            assertCellSelected(tableId, nextCell)
            pressOnInputDialogNext()
            assertCellDisabled(tableId, greyCell2)
            assertCellSelected(tableId, nextLineCell)
        }
    }

    private fun createDataSetInstanceStep(
        orgUnit: String,
        catCombo: String? = null,
        openFuturePeriods: Int? = null,
    ) {
        dataSetDetailRobot(composeTestRule) {
            clickOnAddDataSet()
        }
        dataSetInitialRobot(composeTestRule) {
            checkActionInputIsNotDisplayed()
            clickOnInputOrgUnit()
        }

        orgUnitSelectorRobot(composeTestRule) {
            selectTreeOrgUnit(orgUnit)
        }

        dataSetInitialRobot(composeTestRule) {
            checkActionInputIsNotDisplayed()
            clickOnInputPeriod()
        }

        // wait until the period selector is on the screen
        composeTestRule.waitUntil {
            composeTestRule
                .onNodeWithTag("period_selector")
                .isDisplayed()
        }

        reportPeriodSelectorRobot(composeTestRule) {
            openFuturePeriods
                ?.let { checkFuturePeriodAvailable(it) }

            selectPeriod(openFuturePeriods?: 0)
        }

        dataSetInitialRobot(composeTestRule) {
            catCombo?.let {
                clickOnInputCatCombo()
                selectCatCombo(catCombo)
            }
        }

        dataSetInitialRobot(composeTestRule) {
            checkActionInputIsDisplayed()
            clickOnActionButton()
        }
    }

    private fun createDailyPeriodDataSetInstanceStep(
        orgUnit: String,
        date: String,
        catCombo: String? = null,
    ) {
        dataSetDetailRobot(composeTestRule) {
            clickOnAddDataSet()
        }

        dataSetInitialRobot(composeTestRule) {
            catCombo?.let {
                clickOnInputCatCombo()
                selectCatCombo(catCombo)
            }
            checkActionInputIsNotDisplayed()
            clickOnInputOrgUnit()
            orgUnitSelectorRobot(composeTestRule) {
                selectTreeOrgUnit(orgUnit)
            }
            checkActionInputIsNotDisplayed()
            clickOnInputPeriod()
            chooseDate(date)
            checkActionInputIsDisplayed()
            clickOnActionButton()
        }
    }

    private fun checkMandatoryIconOnCell(
        tableId: String,
        cellId: String,
    ) {
        dataSetTableRobot(composeTestRule) {
            assertCellHasMandatoryIcon(tableId, cellId)
        }
    }
}
