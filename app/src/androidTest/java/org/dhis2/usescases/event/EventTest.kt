package org.dhis2.usescases.event

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
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
    val rule = lazyActivityScenarioRule<EventCaptureActivity>(launchActivity = false)

    @get:Rule
    val ruleTeiDashboard =
        lazyActivityScenarioRule<TeiDashboardMobileActivity>(launchActivity = false)

    @get:Rule
    val ruleEventDetail = lazyActivityScenarioRule<EventInitialActivity>(launchActivity = false)

    @get:Rule
    val eventListRule = lazyActivityScenarioRule<ProgramEventDetailActivity>(launchActivity = false)

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
        val completion = 92
        val email = "mail@mail.com"

        enableComposeForms()

        prepareEventDetailsIntentAndLaunchActivity(rule)

        eventRegistrationRobot {
            checkEventDataEntryIsOpened(completion, email, composeTestRule)
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

    private fun createEventStatusDetails() = EventStatusUIModel(
        "Lab monitoring",
        "Event Completed",
        "1/6/2020",
        "Ngelehun CHC"
    )

    private fun prepareProgramAndLaunchActivity(programUid: String) {
        Intent().apply {
            putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, programUid)
        }.also { eventListRule.launch(it) }
    }

    private fun disableRecyclerViewAnimations() {
        eventListRule.getScenario().onActivity {
            it.runOnUiThread {
                it.supportFragmentManager.findFragmentByTag("EVENT_LIST").apply {
                    (this as EventListFragment).binding.recycler.itemAnimator = null
                }
            }
        }
    }
}