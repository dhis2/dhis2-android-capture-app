package org.dhis2.usescases.datasets

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.flow.syncFlow.robot.dataSetRobot
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
import org.junit.Ignore
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

    @Test
    fun shouldCreateNewDataSet() {
        val period = "Jul 2025"
        val orgUnit = "Ngelehun CHC"
        startDataSetDetailActivity(
            "BfMAe6Itzgt",
            "Child Health",
            ruleDataSetDetail
        )

        dataSetDetailRobot {
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
