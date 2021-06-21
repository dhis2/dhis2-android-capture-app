package org.dhis2.usescases.datasets

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.flow.syncFlow.robot.dataSetRobot
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

        dataSetTableRobot {
            clickOnSaveButton()
            checkActivityHasNotFinished(ruleDataSet.activity)
        }
    }

    @Test
    fun shouldCreateNewDataSet() {
        val period = "Mar 2021"
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
        dataSetTableRobot {
            typeOnEditTextCell("9", 0, 0)
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnNegativeButton()
        }
    }

    @Test
    fun shouldOpenAndEditDataset(){
        startDataSetDetailActivity("ZOV1a5R4gqH", "DS EXTRA TEST",ruleDataSetDetail)

        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }

        dataSetTableRobot {
            typeOnEditTextCell("5", 0, 0)
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnNegativeButton()
        }
    }

    @Ignore("Our testing platform (Browserstack) is rotating device and making it fail")
    @Test
    fun shouldReopenModifyAndCompleteDataset(){
        startDataSetDetailActivity("V8MHeZHIrcP", "Facility Assessment", ruleDataSetDetail)

        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }

        dataSetTableRobot {
            openMenuMoreOptions()
            clickOnMenuReOpen()
            clickOnPositiveButton()
            clickOnEditTextCell(0, 0)
            acceptDateSelected()
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnPositiveButton()
        }
        dataSetDetailRobot {
            checkDataSetIsCompleteAndModified("2019")
        }

    }
}
