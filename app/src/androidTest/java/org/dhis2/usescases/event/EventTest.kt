package org.dhis2.usescases.event

import android.content.Intent
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
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programEventDetail.eventList.EventListFragment
import org.dhis2.usescases.programevent.robot.programEventsRobot
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(EventCaptureActivity::class.java, false, false)

    @get:Rule
    val ruleTeiDashboard = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @get:Rule
    val ruleEventDetail = ActivityTestRule(EventInitialActivity::class.java, false, false)

    @get:Rule
    val eventListRule = ActivityTestRule(ProgramEventDetailActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Ignore
    @Test
    fun shouldDeleteEventWhenClickOnDeleteInsideSpecificEvent() {
        val tbVisitDate = "31/12/2019"
        val tbProgramStages = createProgramStageModel()

        prepareEventToDeleteIntentAndLaunchActivity(ruleTeiDashboard)

        teiDashboardRobot(composeTestRule) {
            clickOnEventGroupByStageUsingDate(tbVisitDate)
        }

        eventRegistrationRobot {
            openMenuMoreOptions()
            clickOnDelete()
            clickOnDeleteDialog()
        }

        teiDashboardRobot(composeTestRule) {
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
            checkEventDetails(eventDetails, composeTestRule)
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

    @Ignore
    @Test
    fun shouldSuccessfullyUpdateAndSaveEvent() {
        val labMonitoring = "Lab monitoring"
        val eventDate = "1/6/2020"
        val radioFormLength = 4

        prepareEventToUpdateIntentAndLaunchActivity(ruleTeiDashboard)

        teiDashboardRobot(composeTestRule) {
            clickOnStageGroup(labMonitoring)
            clickOnEventGroupByStage(eventDate)
        }

        eventRobot(composeTestRule) {
            fillRadioButtonForm(radioFormLength)
            clickOnFormFabButton()
            clickOnCompleteButton()
        }

        teiDashboardRobot(composeTestRule) {
            clickOnStageGroup(labMonitoring)
            checkEventStateStageGroup(labMonitoringStatus)
        }
    }

    @Test
    @Ignore("We need to change configuration in the program")
    fun shouldAvoidLeavingFormWithErrors() {
        val atenatalCare = "lxAQ7Zs9VYR"

        prepareProgramAndLaunchActivity(atenatalCare)
        disableRecyclerViewAnimations()

        programEventsRobot(composeTestRule) {
            clickOnAddEvent()
        }
        eventRegistrationRobot {
            clickNextButton()
        }
        eventRobot(composeTestRule) {
            typeOnRequiredEventForm("125", 1)
            clickOnFormFabButton()
            checkSecondaryButtonNotVisible()
        }
    }

    private val tbVisitProgramStage = createTbVisitStageModel()
    private val labMonitoringProgramStage = createLabMonitoringStageModel()
    private val sputumProgramStage = createSputumStageModel()
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

    private fun prepareProgramAndLaunchActivity(programUid: String) {
        Intent().apply {
            putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, programUid)
        }.also { eventListRule.launchActivity(it) }
    }

    private fun disableRecyclerViewAnimations() {
        val activity = eventListRule.activity
        activity.runOnUiThread {
            activity.supportFragmentManager.findFragmentByTag("EVENT_LIST").apply {
                (this as EventListFragment).binding.recycler.itemAnimator = null
            }
        }
    }
}