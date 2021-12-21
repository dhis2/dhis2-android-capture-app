package org.dhis2.usescases.filters

import androidx.test.rule.ActivityTestRule
import org.dhis2.common.filters.filterRobotCommon
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.flow.syncFlow.robot.eventWithoutRegistrationRobot
import org.dhis2.usescases.form.formRobot
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class FilterTest: BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)


    @Test
    fun checkFromToDateFilter() {
        setupCredentials()
        startActivity()
        setDatePicker()

        homeRobot {
            openFilters()
        }

        filterRobotCommon {
            openFilterAtPosition(0)
            clickOnFromToDateOption()
            selectDate(2020,6,15)
            acceptDateSelected()
            selectDate(2020,11,7)
            acceptDateSelected()
        }
        homeRobot {
            openFilters()
            checkItemsInProgram(4,"Child Programme", "3")
            checkItemsInProgram(6, "Contraceptives Voucher Program", "5")
            checkItemsInProgram(27, "Mortality < 5 years", "4")
        }
        cleanLocalDatabase()
    }

    @Test
    fun checkWritingOrgUnitFilter() {
        setupCredentials()
        startActivity()

        homeRobot {
            openFilters()
        }

        filterRobotCommon {
            openFilterAtPosition(1)
            typeOrgUnit("OU TEST PARENT")
            clickAddOrgUnit()
            closeKeyboard()
        }
        homeRobot {
            openFilters()
            checkItemsInProgram(4,"Child Programme", "0")
            checkItemsInProgram(43, "XX TEST EVENT FULL", "2")
            checkItemsInProgram(45, "XX TEST TRACKER PROGRAM", "4")
        }
        cleanLocalDatabase()
    }

    @Test
    fun checkTreeOrgUnitFilter(){
        startActivity()
        setupCredentials()

        homeRobot {
            openFilters()
        }

        filterRobotCommon {
            openFilterAtPosition(1)
            clickOnOrgUnitTree()
            selectTreeOrgUnit("OU TEST PARENT")
            confirmSelection()
        }
        homeRobot {
            openFilters()
            checkItemsInProgram(4,"Child Programme", "0")
            checkItemsInProgram(43, "XX TEST EVENT FULL", "2")
            checkItemsInProgram(45, "XX TEST TRACKER PROGRAM", "4")
        }
        cleanLocalDatabase()
    }

    @Test
    fun checkSyncFilter() {
        setupCredentials()
        startActivity()

        homeRobot {
            openProgramByPosition(0)
        }
        eventWithoutRegistrationRobot {
            clickOnEventAtPosition(0)
        }
        eventRobot {
            clickOnFormFabButton()
            clickOnFinishAndComplete()
            pressBack()
        }
        homeRobot {
            openFilters()
        }
        filterRobotCommon {
            openFilterAtPosition(2)
            selectNotSyncedState()
        }
        homeRobot {
            checkItemsInProgram(0,"Antenatal care visit", "1")
            checkItemsInProgram(4,"Child Programme", "0")
        }
        cleanLocalDatabase()
    }

    @Ignore("TODO: Review why is failing on browserstack")
    @Test
    fun checkCombinedFilters() {
        setupCredentials()
        startActivity()

        homeRobot {
            openProgramByPosition(41)
        }
        eventWithoutRegistrationRobot {
            clickOnEventAtPosition(0)
        }
        formRobot {
            clickOnSelectOption("ZZ TEST RULE ACTIONS A", 1,"Hide Field", 1)
            pressBack()
            pressBack()
            pressBack()
        }
        homeRobot {
            openFilters()
        }

        filterRobotCommon {
            openFilterAtPosition(1)
            typeOrgUnit("OU TEST PARENT")
            clickAddOrgUnit()
            closeKeyboard()
            openFilterAtPosition(2)
            selectNotSyncedState()
        }
        homeRobot {
            openFilters()
            checkItemsInProgram(37,"TB program", "0")
            waitToDebounce(700)
            checkItemsInProgram(41, "XX TEST EVENT FULL", "1")
            waitToDebounce(700)
        }
        cleanLocalDatabase()
    }



    private fun startActivity() {
        rule.launchActivity(null)
    }
}