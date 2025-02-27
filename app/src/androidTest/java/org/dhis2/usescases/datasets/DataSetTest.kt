package org.dhis2.usescases.datasets

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
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
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class DataSetTest : BaseTest() {

    @get:Rule
    val ruleDataSet = ActivityTestRule(DataSetTableActivity::class.java, false, false)

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

        enableFeatureConfigValue(Feature.COMPOSE_AGGREGATES_SCREEN)
        enterDataSetStep("BfMAe6Itzgt", "Child Health")
        dataSetInstanceInChronologicalOrderStep()
        createDataSetInstanceStep()

        tableIsVisible()

        syncButtonIsAvailableStep()
        checkIndicatorsStep()
        checkTotals()
        enterDataStep()
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


    private suspend fun enterDataSetStep(dataSetUid: String, datasetName: String) {
        startDataSetDetailActivity(
            dataSetUid,
            datasetName,
            ruleDataSetDetail
        )
    }

    private suspend fun dataSetInstanceInChronologicalOrderStep() {
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

    private suspend fun enterDataStep() {
        val cell00Id = "PGRlPnM0Nm01TVMwaHh1Ojxjb2M+UHJsdDBDMVJGMHM="

        dataSetTableRobot(composeTestRule) {
            clickOnCell("dzjKKQq0cSO", cell00Id)
            assertInputDialogIsDisplayed(composeTestRule)
            assertInputDescriptionIsDisplayed("BCG doses administered.")
            typeOnInputDialog("12")
            assertCellHasValue("dzjKKQq0cSO", cell00Id, "12")
            assertRowTotalValue("dzjKKQq0cSO",0, "12.0")
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

    private fun createDataSetInstance(
        orgUnit: String,
        period: String,
    ) {
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
            clickOnActionButton()
        }
    }

    @Ignore("There are no validation rules in the testing database")
    @Test
    fun shouldNotCloseActivityAfterQualityCheckIfDataSetIsComplete() {
        setupCredentials()
        startDataSetActivity(
            "BfMAe6Itzgt",
            "DiszpKrYNg8",
            "202001",
            "HllvX50cXC0",
            ruleDataSet
        )

        dataSetTableRobot(composeTestRule) {
            clickOnSaveButton()
            checkActivityHasNotFinished(ruleDataSet.activity)
        }
    }

    //TODO This test generates a new dataset instance and breaks dataset automation count
    @Test
    fun shouldCreateNewDataSet() {
        val period = "July 2025"
        val orgUnit = "Ngelehun CHC"
        startDataSetDetailActivity(
            "BfMAe6Itzgt",
            "Child Health",
            ruleDataSetDetail
        )

        createDataSetInstance(
            orgUnit = orgUnit,
            period = period,
        )

        dataSetTableRobot(composeTestRule) {
            clickOnCell("dzjKKQq0cSO", 0, 0)
            clickOnEditValue()
            typeInput("1")
            pressBack()
            pressBack()
            clickOnSaveButton()
            clickOnNegativeButton()
            clickOnNegativeButton()
        }

        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }

        dataSetTableRobot(composeTestRule) {
            clickOnCell("dzjKKQq0cSO", 0, 1)
            clickOnEditValue()
            typeInput("5")
            pressBack()
            pressBack()
            clickOnSaveButton()
            clickOnNegativeButton()
            clickOnPositiveButton()
        }
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
}
