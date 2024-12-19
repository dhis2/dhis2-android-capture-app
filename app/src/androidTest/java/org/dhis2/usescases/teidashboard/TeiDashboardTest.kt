package org.dhis2.usescases.teidashboard

import android.annotation.SuppressLint
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.R
import org.dhis2.common.mockwebserver.MockWebServerRobot.Companion.API_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_PATH
import org.dhis2.common.mockwebserver.MockWebServerRobot.Companion.API_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_RESPONSE
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.orgunitselector.orgUnitSelectorRobot
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.entity.EnrollmentUIModel
import org.dhis2.usescases.teidashboard.entity.UpperEnrollmentUIModel
import org.dhis2.usescases.teidashboard.robot.analyticsRobot
import org.dhis2.usescases.teidashboard.robot.enrollmentRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.indicatorsRobot
import org.dhis2.usescases.teidashboard.robot.noteRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.hisp.dhis.android.core.mockwebserver.ResponseController
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class TeiDashboardTest : BaseTest() {

    @get:Rule
    val rule = lazyActivityScenarioRule<TeiDashboardMobileActivity>(launchActivity = false)

    @get:Rule
    val ruleSearch = lazyActivityScenarioRule<SearchTEActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    override fun setUp() {
        super.setUp()
        setupMockServer()
    }

    @Test
    fun shouldSuccessfullyCreateANoteWhenClickCreateNote() {
        setupCredentials()

        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            goToNotes()
        }

        noteRobot {
            clickOnFabAddNewNote()
            typeNote(NOTE_VALID)
            clickOnSaveButton()
            checkNewNoteWasCreated(NOTE_VALID)
        }
    }

    @Test
    fun shouldNotCreateANoteWhenClickClear() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            goToNotes()
        }

        noteRobot {
            clickOnFabAddNewNote()
            typeNote(NOTE_INVALID)
            clickOnClearButton()
            clickYesOnAlertDialog()
            checkNoteWasNotCreated(NOTE_INVALID)
        }
    }

    @Test
    fun shouldOpenNotesDetailsWhenClickOnNote() {
        prepareTeiWithExistingNoteAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            goToNotes()
        }

        noteRobot {
            clickOnFabAddNewNote()
            typeNote(NOTE_EXISTING_TEXT)
            clickOnSaveButton()
            checkNoteDetails("@$USER", NOTE_EXISTING_TEXT)
        }
    }

    @Test
    fun shouldReactivateTEIWhenClickReOpenWithProgramCompletedEvents() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuReOpen()
            checkAllEventsCompleted(1)
        }
    }

    @Test
    fun shouldShowInactiveProgramWhenClickDeactivate() {
        prepareTeiOpenedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuDeactivate()
            checkCancelledStateInfoBarIsDisplay()
            checkCanNotAddEvent()
            checkAllEventsAreClosed()
        }
    }

    @Test
    fun shouldCompleteProgramWhenClickComplete() {
        prepareTeiOpenedForCompleteProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkCompleteStateInfoBarIsDisplay()
            checkCanNotAddEvent()
            checkAllEventsAreClosed()
        }
    }

    @Ignore("next button is sometimes not reached. Review feature.")
    @Test
    fun shouldShowQRWhenClickOnShare() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnShareButton()
            clickOnNextQR()
        }
    }

    @Test
    fun shouldMakeAReferral() {
        prepareTeiOpenedForReferralProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnFab()
            clickOnReferral()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventWasCreated(LAB_MONITORING)
        }
    }

    @Test
    fun shouldSuccessfullyScheduleAnEvent() {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
        val formattedCurrentDate = currentDate.format(formatter)

        prepareTeiOpenedWithNoPreviousEventProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnFab()
            clickOnScheduleNew()
            typeOnInputDateField(formattedCurrentDate, "Due date")
            clickOnSchedule()
            waitToDebounce(1000)
            checkEventWasScheduled(LAB_MONITORING, 0)
        }
    }

    @Test
    fun shouldOpenEventAndSaveSuccessfully() {
        setupCredentials()

        prepareTeiOpenedProgrammeAndLaunchActivity(rule)

        val babyPostNatal = "Baby Postnatal"
        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnEventWithTitle(babyPostNatal)
        }

        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnNotNow()
        }
    }

    @Ignore("This is checking xml instead of compose. Update mobile library with test tags.")
    @Test
    fun shouldShowCorrectInfoWhenOpenTEI() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        val upperInformation = createExpectedUpperInformation()

        teiDashboardRobot(composeTestRule) {
            checkUpperInfo(upperInformation)
        }
    }

    @Test
    fun shouldShowTEIDetailsWhenClickOnSeeDetails() {
        //Adding mock response for API_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_PATH
        //The TEI does not have value assigned for unique ID, as it is autogenerated, it tries to
        //generate one but there are no reserved values in the database so it performs a new request.
        mockWebServerRobot.addResponse(
            method = ResponseController.GET,
            path = API_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_PATH,
            sdkResource = API_TRACKED_ENTITY_ATTRIBUTES_RESERVED_VALUES_RESPONSE,
            responseCode = 200,

            )
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        val enrollmentFullDetails = createExpectedEnrollmentInformation()

        teiDashboardRobot(composeTestRule) {
            clickOnSeeDetails()
            composeTestRule.waitForIdle()
            checkFullDetails(enrollmentFullDetails)
        }
    }

    @Test
    fun shouldShowIndicatorsDetailsWhenClickOnIndicatorsTab() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            goToAnalytics()
            composeTestRule.waitForIdle()
        }

        indicatorsRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            checkDetails("0", "4817")
        }
    }

    @SuppressLint("IgnoreWithoutReason")
    @Ignore
    @Test
    fun shouldOpenEventEditAndSaveSuccessfully() {
        prepareTeiOpenedToEditAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnEventWith(LAB_MONITORING)
            waitToDebounce(600)
        }

        eventRobot(composeTestRule) {
            waitToDebounce(600)
            clickOnFormFabButton()
            clickOnCompleteButton()
            waitToDebounce(600)
        }

        teiDashboardRobot(composeTestRule) {
            checkEventWasCreatedAndClosed(LAB_MONITORING)
        }
    }

    @Test
    fun shouldEnrollToOtherProgramWhenClickOnProgramEnrollments() {
        val womanProgram = "MNCH / PNC (Adult Woman)"
        val personAttribute =
            context.getString(R.string.enrollment_single_section_label).replace("%s", "Person")
        val visitPNCEvent = "PNC Visit"
        val deliveryEvent = "Delivery"
        val visitANCEvent = "ANC Visit (2-4+)"
        val firstANCVisitEvent = "ANC 1st visit"
        val orgUnit = "Ngelehun CHC"

        setDatePicker()
        prepareTeiToEnrollToOtherProgramAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot(composeTestRule) {
            clickOnAProgramForEnrollment(composeTestRule, womanProgram)
            clickOnAcceptInDatePicker()
        }

        orgUnitSelectorRobot(composeTestRule) {
            selectTreeOrgUnit(orgUnit)
        }

        enrollmentRobot(composeTestRule) {
            openFormSection(personAttribute)
            typeOnInputDateField("01012000", "Date of birth")
            clickOnSaveEnrollment()
        }

        teiDashboardRobot(composeTestRule) {
            waitToDebounce(1000)
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            checkEventWasScheduled(visitPNCEvent, 0)
            checkEventWasScheduled(deliveryEvent, 1)
            checkEventWasScheduled(visitANCEvent, 2)
            checkEventWasScheduled(firstANCVisitEvent, 3)
        }
    }


    @Test
    fun shouldShowAnalytics() {
        val chartName = "Daily-TB smear microscopy number of specimen"
        setupCredentials()
        prepareTeiForAnalyticsAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            goToAnalytics()
        }

        indicatorsRobot(composeTestRule) {
            checkGraphIsRendered(chartName)
        }

        analyticsRobot {
            checkGraphType(1, ChartType.LINE_CHART)
        }
    }

    private fun createExpectedUpperInformation() =
        UpperEnrollmentUIModel(
            "10/1/2024",
            "10/1/2021",
            "Ngelehun CHC"
        )

    private fun createExpectedEnrollmentInformation() =
        EnrollmentUIModel(
            "10/01/2025",
            "10/01/2025",
            "Filona",
            "Ryder",
            "Female"
        )

    companion object {
        const val NOTE_VALID = "ThisIsJustATest"
        const val NOTE_INVALID = "InvalidNote"
        const val NOTE_EXISTING_TEXT = "This is a note test"
        const val USER = "android"

        const val LAB_MONITORING = "Lab monitoring"
    }
}
