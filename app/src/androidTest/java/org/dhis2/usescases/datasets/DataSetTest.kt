package org.dhis2.usescases.datasets

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import android.util.Log
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.dataSetTable.period.reportPeriodSelectorRobot
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.flow.syncFlow.robot.dataSetRobot
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
import org.dhis2.usescases.searchte.robot.filterRobot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class DataSetTest : BaseTest() {
    @get:Rule
    val ruleDataSetDetail = lazyActivityScenarioRule<DataSetDetailActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    private val featureConfigRepository: FeatureConfigRepository = mock()


    override fun teardown() {
        super.teardown()
        disableFeatureConfigValue(Feature.COMPOSE_AGGREGATES_SCREEN)
        cleanLocalDatabase()
    }

    @Test
    fun datasetAutomate() = runTest {
        val period = "Jul 2025"
        val orgUnit = "Ngelehun CHC"

        enableFeatureConfigValue(Feature.COMPOSE_AGGREGATES_SCREEN)

        enterDataSetStep(
            uid = "BfMAe6Itzgt",
            name = "Child Health",
        )
        dataSetInstanceInChronologicalOrderStep()
        createDataSetInstanceStep(
            period = period,
            orgUnit = orgUnit,
        )

        tableIsVisible()

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
        reenterDataSetToCheckValueSavedStep()


        // Step - Test combination of filters - TODO Move the step after creating dataset instance
        // ORG unit add some dataset instance out of Ngelahun CHC to filter by Ngelahun CHC
        // Period filter from - to specific period where instances exist
        // Sync move after create dataset instance and check the filter afterwards
        // checkFilterCombination(orgUnit)
    }

    @Test
    fun formConfigurationTestAutomate() = runTest {
        enableFeatureConfigValue(Feature.COMPOSE_AGGREGATES_SCREEN)
        // Start Activity
        enterDataSetStep("DMicXfEri6s", "Form configuration options")


        // Step - ANDROAPP-6795 Check content boxes above and below the table
        checkContentBoxesAreDisplayed()
        // Step - ANDROAPP-6810 Move a category to rows (click on sections 8, 16, 24)
        // Step - ANDROAPP-6828 Automatic grouping (click on sections 19, 20, 22)
        // Step - ANDROAPP-6811 Pivot options (click on sections 5, 13, 23)
    }


    private suspend fun checkContentBoxesAreDisplayed() {
        composeTestRule.awaitIdle()
        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }
        tableIsVisible()
        // Check top and bottom content is displayed in initial section
        dataSetDetailRobot(composeTestRule) {
            assertItemWithTextIsDisplayed("CONTENT BEFORE 1:", true)
        }
        dataSetTableRobot(composeTestRule) {
            scrollToItem(2)
            assertItemWithTextIsDisplayed("CONTENT AFTER 1:", true)
        }
        // Check top and bottom content is displayed when changing sections
        dataSetDetailRobot(composeTestRule) {
            clickOnSection("TAB_2")
        }
        composeTestRule.awaitIdle()
        // Check top and bottom content is displayed when changing sections
        dataSetDetailRobot(composeTestRule) {
            assertItemWithTextIsDisplayed("CONTENT BEFORE 2:", true)
        }
        dataSetTableRobot(composeTestRule) {
            scrollToItem(2)
            assertItemWithTextIsDisplayed("CONTENT AFTER 2:", true)
        }
    }

    @Test
    fun saveAndCompleteMandatoryFieldMandatoryValidationRule() = runBlocking {
        val dataSetUid = "Lpw6GcnTrmS"
        val dataSetName = "Emergency Response"
        val period = "Jan 2025"
        val orgUnit = "Ngelehun CHC"
        val catCombo = "Result"
        val tableId = "bjDvmb4bfuf"
        val cellValidationRuleId = "PGRlPktGbkZwYnFEcWppOjxjb2M+SGxsdlg1MGNYQzA="
        val cellMandatoryId = "PGRlPnpGRmIzYmFyNEN0Ojxjb2M+SGxsdlg1MGNYQzA="

        enableFeatureConfigValue(Feature.COMPOSE_AGGREGATES_SCREEN)

        enterDataSetStep(
            uid = dataSetUid,
            name = dataSetName,
        )

        createDataSetInstanceStep(
            period = period,
            orgUnit = orgUnit,
            catCombo = catCombo,
        )

        tapOnDoneButtonStep()

        checkValidationBarIsDisplayed()

        enterDataStep(
            tableId = tableId,
            cellId = cellValidationRuleId,
            value = "1",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        tapOnDoneButtonStep()

        checkCompleteDialogIsDisplayedAndAttemptToCompleteStep()

        checkMandatoryDialogIsDisplayedAndAcceptStep()

        enterDataStep(
            tableId = tableId,
            cellId = cellMandatoryId,
            value = "2",
            inputTestTag = "INPUT_NUMBER_FIELD"
        )

        tapOnDoneButtonStep()

        checkCompleteDialogIsDisplayedAndAttemptToCompleteStep()

        checkDataSetInstanceHasBeenCreated(period, orgUnit)
    }

    private fun checkValidationBarIsDisplayed() {
        logStep("Starting Check Validation Rule errors")

        dataSetTableRobot(composeTestRule) {
            assertValidationBarIsDisplayed()
            expandValidationRulesErrorDialog()
            tapOnReview()
        }
        logStep("Finished Check Validation Rule errors")
    }

    private fun checkDataSetInstanceHasBeenCreated(
        period: String,
        orgUnit: String,
    ) {
        logStep("Starting Check dataset instance has been created")
        dataSetDetailRobot(composeTestRule) {
            checkDataSetInList(period, orgUnit)
        }
        logStep("Finished Check dataset instance has been created")
    }

    private fun checkMandatoryDialogIsDisplayedAndAcceptStep() {
        logStep("Starting Checking Mandatory Dialog")
        dataSetTableRobot(composeTestRule) {
            checkMandatoryDialogIsDisplayed()
            acceptMandatoryDialog()
        }
        logStep("Finished Checking Mandatory Dialog")
    }

    private fun checkCompleteDialogIsDisplayedAndAttemptToCompleteStep() {
        logStep("Starting Trying to complete dataset")

        dataSetTableRobot(composeTestRule) {
            checkCompleteDialogIsDisplayed()
            tapOnCompleteButton()
        }
        logStep("Finished Trying to complete dataset")
    }

    private fun tapOnDoneButtonStep() {
        logStep("Starting Tap on Done button")

        dataSetTableRobot(composeTestRule) {
            tapOnSaveButton()
        }
        logStep("Finished Tap on Done button")
    }

    private fun enterDataSetStep(
        uid: String,
        name: String,
    ) {
        logStep("Starting Entering dataset $name")
        startDataSetDetailActivity(
            dataSetUid = uid,
            dataSetName = name,
            rule = ruleDataSetDetail
        )
        logStep("Finished Entering dataset $name")
    }

    private fun dataSetInstanceInChronologicalOrderStep() {
        dataSetDetailRobot(composeTestRule) {
            checkDatasetListIsSortedChronologically()
        }
    }

    private suspend fun createDataSetInstanceStep() {
        val period = "July 2025"
        val orgUnit = "Ngelehun CHC"

        createDataSetInstance(
            orgUnit = orgUnit,
            period = period,
        )
    }

    private suspend fun tableIsVisible() {
        composeTestRule.awaitIdle()
        dataSetTableRobot(composeTestRule) {
            assertTableIsDisplayed()
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
                totalColumnHeaderColumnIndex = 2,
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
        logStep("Starting Enter value: $value into cell ${dataElementDescription ?: cellId}")

        dataSetTableRobot(composeTestRule) {
            clickOnCell(tableId, cellId)
            assertInputDialogIsDisplayed()
            dataElementDescription?.let {
                assertInputDescriptionIsDisplayed(dataElementDescription)
            }
            typeOnInputDialog(value, inputTestTag)
            assertCellHasValue(tableId, cellId, value)
        }

        logStep("Finished Enter value: $value into cell ${dataElementDescription ?: cellId}")
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
        tableIsVisible()

        dataSetTableRobot(composeTestRule) {
            assertCellHasValue("dzjKKQq0cSO", cell00Id, "12")
        }
    }

    private fun checkFilterCombination(
        orgUnit: String,
    ) {
        filterRobot(composeTestRule) {
            //Open filter
            openFilters()

            //Filter by org unit Ngelehun CHC
            clickOnFilterBy(filter = "ORG. UNIT")
//            clickOnSortByField(orgUnitFilter) this icons are not visible but can b e pressed do we need them in dataset?
            typeOrgUnitField(orgUnit)
            checkFilterCounter("1")
        }

        dataSetDetailRobot(composeTestRule) {
            assertEquals(11, getListItemCount())
        }

        filterRobot(composeTestRule) {
            //Filter by period Last Month
            clickOnFilterBy(filter = "Period")
            clickOnLastMonthPeriodFilter()
            checkFilterCounter("2")

            clickOnAnytimePeriodFilter()
            checkFilterCounter("1")
        }

        dataSetDetailRobot(composeTestRule) {
            assertEquals(11, getListItemCount())
        }
    }

    private fun createDataSetInstanceStep(
        orgUnit: String,
        period: String,
        catCombo: String? = null,
    ) {
        logStep("Starting Creating dataset instance $period")

        dataSetDetailRobot(composeTestRule) {
            clickOnAddDataSet()
        }
        dataSetInitialRobot {
            clickOnInputOrgUnit()
        }

        orgUnitSelectorRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            selectTreeOrgUnit(orgUnit)
        }

        dataSetInitialRobot {
            clickOnInputPeriod()
        }

        reportPeriodSelectorRobot(composeTestRule) {
            selectReportPeriod(period)
        }

        dataSetInitialRobot {
            catCombo?.let {
                clickOnInputCatCombo()
                selectCatCombo(catCombo)
            }
        }

        dataSetInitialRobot {
            clickOnActionButton()
        }
        logStep("Finished Creating dataset instance $period")
    }

    @Test
    fun shouldSelectNewCellIfCurrentHasNoErrorAndBlockSelectingNewCellIfCurrentHasError() {
        startDataSetDetailActivity("BfMAe6Itzgt", "Child Health", ruleDataSetDetail)

        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }

        dataSetTableRobot(composeTestRule) {
            clickOnCell("dzjKKQq0cSO", 0, 0)
            clickOnEditValue()
            typeInput("5")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performImeAction()
            assertCellSelected("dzjKKQq0cSO", 0, 1)

            clickOnEditValue()
            typeInput("5,,")
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performImeAction()
            assertCellSelected("dzjKKQq0cSO", 0, 1)
        }
    }

    private fun logStep(message: String) {
        Log.d("DataSetTest", message)
    }
}
