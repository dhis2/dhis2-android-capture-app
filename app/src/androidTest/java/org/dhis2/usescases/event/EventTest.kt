package org.dhis2.usescases.event

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.AppTest.Companion.DB_TO_IMPORT
import org.dhis2.common.filters.filterRobotCommon
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.event.entity.EventStatusUIModel
import org.dhis2.usescases.event.entity.ProgramStageUIModel
import org.dhis2.usescases.event.entity.TEIProgramStagesUIModel
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.flow.syncFlow.robot.eventWithoutRegistrationRobot
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programevent.robot.programEventsRobot
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.TeiDashboardTest.Companion.NOTE_INVALID
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.noteRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import org.dhis2.usescases.eventsWithoutRegistration.*
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
import org.dhis2.usescases.teidashboard.TeiDashboardTest.Companion.NOTE_EXISTING_TEXT


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

        eventRegistrationRobot(composeTestRule) {
            openMenuMoreOptions()
            clickOnDelete()
            clickOnDeleteDialog()
        }

        teiDashboardRobot(composeTestRule) {
            checkEventWasDeletedStageGroup(tbProgramStages)
        }
    }

    @Test
    fun eventFlowTest1(){
        val antenatalCare = "lxAQ7Zs9VYR"
        val completion = 100
        val closedOrgUnit = "Faabu CHP"
        val orgUnit = "Ngelehun CHC"
        val currentDate = LocalDate.now()
        val futureDate = currentDate.plusDays(2)

        //prepareEventDetailsIntentAndLaunchActivity(rule)
        prepareProgramAndLaunchActivity(antenatalCare)

        eventWithoutRegistrationRobot(composeTestRule) {
            //ANDROAPP-851 - Select an event to test - completed events expiry day
            clickOnEventAtPosition(2)

            eventRegistrationRobot(composeTestRule) {
                // ANDROAPP-853 - Check if event is uneditable
                //@todo - improve
                checkIsEditable()
                // ANDROAPP-4836 - Check save button is unavailable
                formActionButtonUnavailable()
                clickGoBack()
            }
        }

        programEventsRobot(composeTestRule) {
            clickOnAddEvent()
        }
        orgUnitSelectorRobot(composeTestRule) {
            selectTreeOrgUnit(closedOrgUnit)
        }
        //@todo - all fields disabled except org unit


        eventRegistrationRobot(composeTestRule) {
            clickGoBack()
            eventRobot(composeTestRule) {
                clickOnNotNow()
            }
        }

        programEventsRobot(composeTestRule) {
            clickOnAddEvent()
        }
        orgUnitSelectorRobot(composeTestRule) {
            selectTreeOrgUnit(orgUnit)
        }
        //@todo - click on date field and enter future date
        //ANDROAPP-906 - enter future date

        //filterRobotCommon {
        //    val day = currentDate.dayOfMonth
        //    val month = currentDate.monthValue
        //    val year = currentDate.year
        //    selectDate(year, month, day)
        //}

        eventRegistrationRobot(composeTestRule) {
            // ANDROAPP-4836 - Check save button is unavailable
            //formActionButtonUnavailable()
            // ANDROAPP-4836 - Check Refresh button is available
            //syncButtonAvaialble()
            clickGoBack()
            eventRobot(composeTestRule) {
                clickOnNotNow()
            }
        }

        eventWithoutRegistrationRobot(composeTestRule) {
            clickOnEventAtPosition(0)
            eventRegistrationRobot(composeTestRule) {
                // ANDROAPP-4836 - Check save button is unavailable
                //formActionButtonUnavailable()
                // ANDROAPP-4836 - Check Refresh button is available
                syncButtonAvaialble()
            }
        }

        //ANDROAPP-4171 - Create and Save note
        teiDashboardRobot(composeTestRule) {
            goToNotes()
        }

        noteRobot {
            clickOnFabAddNewNote()
            typeNote(NOTE_EXISTING_TEXT)
            clickOnClearButton()
            clickYesOnAlertDialog()
            checkNoteWasNotCreated(NOTE_EXISTING_TEXT)
        }

        //ANDROAPP-1543 - Delete Event
        eventRobot(composeTestRule) {
            openMenuMoreOptions()
            clickOnDelete()
            clickOnDeleteDialog()
        }
        programEventsRobot(composeTestRule) {
            checkEventWasDeleted("07/04/2024")
        }
    }

    @Test
    fun shouldShareQRWhenClickOnShare() {
        val qrList = 3

        prepareEventToShareIntentAndLaunchActivity(ruleEventDetail)

        eventRegistrationRobot(composeTestRule) {
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
//            fillRadioButtonForm(radioFormLength)
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

        programEventsRobot(composeTestRule) {
            clickOnAddEvent()
        }
        eventRegistrationRobot(composeTestRule) {
            clickNextButton()
        }
        eventRobot(composeTestRule) {
//            typeOnRequiredEventForm("125", 1)
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
        Intent(
            ApplicationProvider.getApplicationContext(),
            ProgramEventDetailActivity::class.java,
        ).apply {
            putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, programUid)
        }.also { rule.launch(it) }
    }
}