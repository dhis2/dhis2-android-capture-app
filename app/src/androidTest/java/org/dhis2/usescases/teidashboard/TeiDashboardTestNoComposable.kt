package org.dhis2.usescases.teidashboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
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
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @Test
    fun shouldSuccessfullyCreateRelationshipWhenClickAdd() {
        val teiName = "Tim"
        val teiLastName = "Johnson"
        val relationshipName = "Filona"
        val relationshipLastName = "Ryder"
        val completeName = "Ryder Filona"

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            goToRelationships()
        }

        relationshipRobot {
            clickOnFabAdd()
            waitToDebounce(500)
            clickOnRelationshipType()
            waitToDebounce(500)
        }

        searchTeiRobot {
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
}