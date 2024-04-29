package org.dhis2.usescases.teidashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teidashboard.robot.relationshipRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeiDashboardTestNoComposable : BaseTest() {

    @get:Rule
    val ruleSearch = lazyActivityScenarioRule<SearchTEActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Ignore
    @Test
    fun shouldSuccessfullyCreateRelationshipWhenClickAdd() {
        val teiName = "Tim"
        val teiLastName = "Johnson"
        val relationshipName = "Filona"
        val relationshipLastName = "Ryder"
        val completeName = "Ryder Filona"

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot(composeTestRule) {
            clickOnTEI(teiName, teiLastName)
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
            typeAttributeAtPosition(relationshipName, 0)
            typeAttributeAtPosition(relationshipLastName, 1)
            clickOnSearch()
            waitToDebounce(5000)
            clickOnTEI(relationshipName, relationshipLastName)
        }

        relationshipRobot {
            checkRelationshipWasCreated(0, completeName)
        }
    }

    @Test
    fun shouldDeleteTeiSuccessfully() {
        val teiName = "Gertrude"
        val teiLastName = "Fjordsen"
        val firstNamePosition = 0
        val lastNamePosition = 1

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot(composeTestRule) {
            clickOnOpenSearch()
            typeAttributeAtPosition(teiName, firstNamePosition)
            typeAttributeAtPosition(teiLastName, lastNamePosition)
            clickOnSearch()
            clickOnTEI(teiName, teiLastName)
            //scrollToTEIandClick()
        }

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuDeleteTEI()
        }

        searchTeiRobot(composeTestRule) {
            checkTEIsDelete(teiName, teiLastName)
        }
    }

    @Test
    fun shouldDeleteEnrollmentSuccessfully() {
        val teiName = "Anna"
        val teiLastName = "Jones"
        val firstNamePosition = 0
        val lastNamePosition = 1

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot(composeTestRule) {
            clickOnOpenSearch()
            typeAttributeAtPosition(teiName, firstNamePosition)
            typeAttributeAtPosition(teiLastName, lastNamePosition)
            clickOnSearch()
            //     waitToDebounce(400)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnMenuDeleteEnrollment()
        }

        searchTeiRobot(composeTestRule) {
            checkTEIsDelete(teiName, teiLastName)
        }
    }
}