package org.dhis2.usescases.filters

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.ActivityTestRule
import org.dhis2.common.filters.filterRobotCommon
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.flow.syncFlow.robot.eventWithoutRegistrationRobot
import org.dhis2.usescases.form.formRobot
import org.dhis2.usescases.main.AVOID_SYNC
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class FilterTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createComposeRule()

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
            selectDate(2020, 6, 15)
            acceptDateSelected()
            selectDate(2020, 11, 7)
            acceptDateSelected()
        }
        homeRobot {
            openFilters()
            checkItemsInProgram(composeTestRule, 3, "Child Programme", "3")
            checkItemsInProgram(composeTestRule, 5, "Contraceptives Voucher Program", "5")
            checkItemsInProgram(composeTestRule, 26, "Mortality < 5 years", "4")
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
            checkItemsInProgram(composeTestRule, 3, "Child Programme", "0")
            checkItemsInProgram(composeTestRule, 41, "XX TEST EVENT FULL", "2")
            checkItemsInProgram(composeTestRule, 43, "XX TEST TRACKER PROGRAM", "4")
        }
        cleanLocalDatabase()
    }

    @Test
    fun checkTreeOrgUnitFilter() {
        startActivity()
        setupCredentials()

        homeRobot {
            openFilters()
        }

        filterRobotCommon {
            openFilterAtPosition(1)
            clickOnOrgUnitTree()
            orgUnitSelectorRobot(composeTestRule) {
                selectTreeOrgUnit("OU TEST PARENT")
            }
        }
        homeRobot {
            openFilters()
            checkItemsInProgram(composeTestRule, 3, "Child Programme", "0")
            checkItemsInProgram(composeTestRule, 41, "XX TEST EVENT FULL", "2")
            checkItemsInProgram(composeTestRule, 43, "XX TEST TRACKER PROGRAM", "4")
        }
        cleanLocalDatabase()
    }

    @Ignore("Undeterministic")
    @Test
    fun checkSyncFilter() {
        setupCredentials()
        startActivity()

        homeRobot {
            openProgramByPosition(composeTestRule, 0)
            waitToDebounce(700)
        }
        eventWithoutRegistrationRobot(composeTestRule) {
            clickOnEventAtPosition(0)
        }
        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnCompleteButton()
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
            openFilters()
            waitToDebounce(1000)
            checkItemsInProgram(composeTestRule, 0, "Antenatal care visit", "1")
            checkItemsInProgram(composeTestRule, 3, "Child Programme", "0")
        }
        cleanLocalDatabase()
    }

    @Ignore("TODO: Review why is failing on browserstack")
    @Test
    fun checkCombinedFilters() {
        setupCredentials()
        startActivity()

        homeRobot {
            openProgramByPosition(composeTestRule, 41)
        }
        eventWithoutRegistrationRobot(composeTestRule) {
            clickOnEventAtPosition(0)
        }
        formRobot {
            clickOnSelectOption("ZZ TEST RULE ACTIONS A", 1, "Hide Field", 1)
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
            checkItemsInProgram(composeTestRule, 37, "TB program", "0")
            waitToDebounce(700)
            checkItemsInProgram(composeTestRule, 41, "XX TEST EVENT FULL", "1")
            waitToDebounce(700)
        }
        cleanLocalDatabase()
    }

    private fun startActivity() {
        val intent = Intent().putExtra(AVOID_SYNC, true)
        rule.launchActivity(intent)
    }
}