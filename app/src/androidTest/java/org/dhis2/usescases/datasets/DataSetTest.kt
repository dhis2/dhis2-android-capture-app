package org.dhis2.usescases.datasets

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.flow.syncFlow.robot.dataSetRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataSetTest : BaseTest() {

    @get:Rule
    val ruleDataSet = ActivityTestRule(DataSetTableActivity::class.java, false, false)

    @get:Rule
    val ruleDataSetDetail = ActivityTestRule(DataSetDetailActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldNotCloseActivityAfterQualityCheckIfDataSetIsComplete() {
        setupCredentials()
        startDataSetActivity(
            "BfMAe6Itzgt",
            "DiszpKrYNg8",
            "Ngelehun CHC",
            "periodName",
            "",
            "202001",
            "HllvX50cXC0",
            ruleDataSet
        )

        dataSetTableRobot(composeTestRule) {
            clickOnSaveButton()
            checkActivityHasNotFinished(ruleDataSet.activity)
        }
    }

    @Test
    fun shouldCreateNewDataSet() {
        val period = "Mar 2022"
        val orgUnit = "Ngelehun CHC"
        startDataSetDetailActivity("ZOV1a5R4gqH", "DS EXTRA TEST", ruleDataSetDetail)

        dataSetDetailRobot {
            clickOnAddDataSet()
        }
        dataSetInitialRobot {
            clickOnInputOrgUnit()
            selectOrgUnit(orgUnit)
            clickOnAcceptButton()
            clickOnInputPeriod()
            selectPeriod(period)
            clickOnActionButton()
        }
        dataSetTableRobot(composeTestRule) {
            typeOnCell("bjDvmb4bfuf", 0, 0)
            clickOnEditValue()
            typeInput("1")
            pressBack()
            composeTestRule.waitForIdle()
            pressBack()
            composeTestRule.waitForIdle()
            pressBack()
            composeTestRule.waitForIdle()
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnNegativeButton()
        }
    }

    @Test
    fun shouldOpenAndEditDataset() {
        startDataSetDetailActivity("ZOV1a5R4gqH", "DS EXTRA TEST", ruleDataSetDetail)

        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }

        dataSetTableRobot(composeTestRule) {
            typeOnCell("bjDvmb4bfuf", 0, 0)
            clickOnEditValue()
            typeInput("5")
            pressBack()
            composeTestRule.waitForIdle()
            pressBack()
            composeTestRule.waitForIdle()
            pressBack()
            composeTestRule.waitForIdle()
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnNegativeButton()
        }
    }

    @Test
    fun shouldReopenModifyAndCompleteDataset() {
        startDataSetDetailActivity("V8MHeZHIrcP", "Facility Assessment", ruleDataSetDetail)

        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }

        dataSetTableRobot(composeTestRule) {
            openMenuMoreOptions()
            clickOnMenuReOpen()
            clickOnPositiveButton()
            typeOnCell("bjDvmb4bfuf", 0, 0)
            clickOnAcceptDate()
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnPositiveButton()
        }
        dataSetDetailRobot {
            checkDataSetIsCompleteAndModified("2019")
        }

    }
}
