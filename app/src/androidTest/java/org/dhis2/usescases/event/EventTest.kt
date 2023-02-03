package org.dhis2.usescases.event

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.event.entity.EventDetailsUIModel
import org.dhis2.usescases.event.entity.EventStatusUIModel
import org.dhis2.usescases.event.entity.ProgramStageUIModel
import org.dhis2.usescases.event.entity.TEIProgramStagesUIModel
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTest: BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(EventCaptureActivity::class.java, false, false)

    @get:Rule
    val ruleTeiDashboard = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @get:Rule
    val ruleEventDetail = ActivityTestRule(EventInitialActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDeleteEventWhenClickOnDeleteInsideSpecificEvent() {
        val tbVisit = "TB visit"
        val tbVisitDate = "31/12/2019"
        val tbProgramStages = createProgramStageModel()

        prepareEventToDeleteIntentAndLaunchActivity(ruleTeiDashboard)

        teiDashboardRobot {
            clickOnStageGroup(tbVisit)
            clickOnEventGroupByStage(tbVisitDate)
        }

        eventRegistrationRobot {
            openMenuMoreOptions()
            clickOnDelete()
            clickOnDeleteDialog()
        }

        teiDashboardRobot {
            checkEventWasDeletedStageGroup(tbProgramStages)
        }
    }

    @Test
    fun shouldShowEventDetailsWhenClickOnDetailsInsideSpecificEvent() {
        val eventDetails = createEventDetails()

        prepareEventDetailsIntentAndLaunchActivity(rule)

        eventRegistrationRobot {
            checkEventFormDetails(eventDetails)
            clickOnDetails()
            checkEventDetails(eventDetails)
        }
    }

    @Test
    fun shouldShareQRWhenClickOnShare() {
        val qrList = 3

        prepareEventToShareIntentAndLaunchActivity(ruleEventDetail)

        eventRegistrationRobot {
            openMenuMoreOptions()
            clickOnShare()
            clickOnAllQR(qrList)
        }
    }

    @Test
    @Ignore
    fun shouldSuccessfullyUpdateAndSaveEvent() {
        val labMonitoring = "Lab monitoring"
        val eventDate = "1/6/2020"
        val radioFormLength = 4

        prepareEventToUpdateIntentAndLaunchActivity(ruleTeiDashboard)

        teiDashboardRobot {
            clickOnStageGroup(labMonitoring)
            clickOnEventGroupByStage(eventDate)
        }

        eventRobot {
            fillRadioButtonForm(radioFormLength)
            clickOnFormFabButton()
            clickOnCompleteButton(composeTestRule)
        }

        teiDashboardRobot {
            clickOnStageGroup(labMonitoring)
            checkEventStateStageGroup(labMonitoringStatus)
        }
    }

    private val tbVisitProgramStage =  createTbVisitStageModel()
    private val labMonitoringProgramStage =  createLabMonitoringStageModel()
    private val sputumProgramStage =  createSputumStageModel()
    private val labMonitoringStatus = createEventStatusDetails()

    private fun createProgramStageModel() = TEIProgramStagesUIModel(
        labMonitoringProgramStage,
        tbVisitProgramStage,
        sputumProgramStage
    )

    private fun createTbVisitStageModel() = ProgramStageUIModel(
        "TB visit",
        "0 events"
    )

    private fun createLabMonitoringStageModel() = ProgramStageUIModel(
        "Lab monitoring",
        "4 events"
    )

    private fun createSputumStageModel() = ProgramStageUIModel(
        "Sputum smear microscopy test",
        "4 events"
    )

    private fun createEventDetails() = EventDetailsUIModel(
        "Alfa",
        91,
        "1/3/2020",
        "OU TEST PARENT"
    )

    private fun createEventStatusDetails() = EventStatusUIModel(
        "Lab monitoring",
        "Event Completed",
        "1/6/2020",
        "Ngelehun CHC"
    )
}