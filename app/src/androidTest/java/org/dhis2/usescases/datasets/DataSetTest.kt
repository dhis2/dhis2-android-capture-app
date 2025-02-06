package org.dhis2.usescases.datasets

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.flow.syncFlow.robot.dataSetRobot
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
import org.dhis2.usescases.searchte.robot.filterRobot
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataSetTest : BaseTest() {

    @get:Rule
    val ruleDataSet = ActivityTestRule(DataSetTableActivity::class.java, false, false)

    @get:Rule
    val ruleDataSetDetail = lazyActivityScenarioRule<DataSetDetailActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    override fun teardown() {
        super.teardown()
        disableFeatureConfigValue(Feature.COMPOSE_AGGREGATES_SCREEN)
        cleanLocalDatabase()
    }

    @Test
    fun datasetAutomate() {
        val period = "Jul 2025"
        val orgUnit = "Ngelehun CHC"

        enableFeatureConfigValue(Feature.COMPOSE_AGGREGATES_SCREEN)

        //Open Dataset
        startDataSetDetailActivity(
            "BfMAe6Itzgt",
            "Child Health",
            ruleDataSetDetail
        )

        //Step - Dataset list is in chronological order
        dataSetDetailRobot(composeTestRule) {
            checkDatasetListIsSortedChronologically()
        }

        //Step - Test combination of filters
        checkFilterCombination(orgUnit)

        //Step - Create dataset instance
        createDataSetInstance(
            orgUnit = orgUnit,
            period = period,
        )
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
            selectTreeOrgUnit(orgUnit)
        }

        dataSetInitialRobot {
            clickOnInputPeriod()
            selectPeriod(period)
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
        val period = "Jul 2025"
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
            typeOnCell("dzjKKQq0cSO", 0, 0)
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
            typeOnCell("dzjKKQq0cSO", 0, 1)
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
            typeOnCell("dzjKKQq0cSO", 0, 0)
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
