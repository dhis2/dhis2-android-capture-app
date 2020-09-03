package org.dhis2.usescases.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.event.entity.EventDetailsUIModel
import org.dhis2.usescases.event.entity.ProgramStageUIModel
import org.dhis2.usescases.event.entity.TEIProgramStagesUIModel
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
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

    @Test
    fun shouldDeleteEventWhenClickOnDeleteInsideSpecificEvent() {
        val tbVisit = "TB visit"
        val tbProgramStages = createProgramStageModel()

        prepareEventToDeleteIntentAndLaunchActivity(ruleTeiDashboard)

        teiDashboardRobot {
            clickOnStageGroup(tbVisit)
            clickOnEventGroupByStage(tbVisit)
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
            openMenuMoreOptions()
            clickOnDetails()
            checkEventDetails(eventDetails)
        }
    }

    @Test
    fun shouldShareQRWhenClickOnShare() {
        val qrList = 3

        prepareEventToShareIntentAndLaunchActivity(ruleEventDetail)

        eventRegistrationRobot {
            clickOnShare()
            clickOnAllQR(qrList)
        }
    }

    private val tbVisitProgramStage =  createTbVisitStageModel()
    private val labMonitoringProgramStage =  createLabMonitoringStageModel()
    private val sputumProgramStage =  createSputumStageModel()

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
        96,
        "1/3/2020",
        "OU TEST PARENT"
    )

}