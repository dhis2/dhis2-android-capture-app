package org.dhis2.usescases.teidashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teidashboard.robot.relationshipRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeiDashboardTestNoComposable : BaseTest() {

    @get:Rule
    val ruleSearch = lazyActivityScenarioRule<SearchTEActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldSuccessfullyCreateRelationshipWhenClickAdd() {
        val teiName = "Tim"
        val teiLastName = "Johnson"
        val relationshipName = "Filona"
        val relationshipLastName = "Ryder"
        val completeName = "Filona Ryder"

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot(composeTestRule) {
            clickOnTEI(teiName)
        }

        teiDashboardRobot(composeTestRule) {
            goToRelationships()
        }

        relationshipRobot {
            clickOnFabAdd()
            waitToDebounce(500)
            clickOnRelationshipType()
            waitToDebounce(500)
        }

        searchTeiRobot(composeTestRule) {
            clickOnOpenSearch()
            openNextSearchParameter("First name")
            typeOnNextSearchTextParameter(relationshipName)
            openNextSearchParameter("Last name")
            typeOnNextSearchTextParameter(relationshipLastName)
            clickOnSearch()
            clickOnTEI(relationshipName)
        }
    }

    @Test
    fun shouldDeleteTeiSuccessfully() {
        val teiName = "Gertrude"
        val teiLastName = "Fjordsen"

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot(composeTestRule) {
            clickOnOpenSearch()
            openNextSearchParameter("First name")
            typeOnNextSearchTextParameter(teiName)
            openNextSearchParameter("Last name")
            typeOnNextSearchTextParameter(teiLastName)
            clickOnSearch()
            clickOnTEI(teiName)
        }

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuDeleteTEI()
            clickOnConfirmDeleteTEI()
        }

        searchTeiRobot(composeTestRule) {
            checkTEIsDelete(teiName, teiLastName)
        }
    }

    @Test
    fun shouldDeleteEnrollmentSuccessfully() {
        val teiName = "Anna"
        val teiLastName = "Jones"

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot(composeTestRule) {
            clickOnOpenSearch()
            openNextSearchParameter("First name")
            typeOnNextSearchTextParameter(teiName)
            openNextSearchParameter("Last name")
            typeOnNextSearchTextParameter(teiLastName)
            clickOnSearch()
            clickOnTEI(teiName)
        }

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuDeleteEnrollment()
            clickOnConfirmDeleteEnrollment()
        }

        searchTeiRobot(composeTestRule) {
            checkTEIsDelete(teiName, teiLastName)
        }
    }
}