package org.dhis2.usescases.teidashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.R
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
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
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeiDashboardTest : BaseTest() {

    @get:Rule
    val rule = lazyActivityScenarioRule<TeiDashboardMobileActivity>(launchActivity = false)

    @get:Rule
    val ruleSearch = lazyActivityScenarioRule<SearchTEActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

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
            clickOnNoteWithPosition(0)
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
            clickOnReferralOption(context.getString(R.string.one_time))
            clickOnReferralNextButton()
            checkEventWasCreated(LAB_MONITORING)
        }
    }

    @Test
    fun shouldSuccessfullyScheduleAnEvent() {
        prepareTeiOpenedWithNoPreviousEventProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnFab()
            clickOnScheduleNew()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventWasCreatedWithDate(LAB_MONITORING, LAB_MONITORING_SCHEDULE_DATE)
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
            scrollToBottomForm()
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
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        val enrollmentFullDetails = createExpectedEnrollmentInformation()

        teiDashboardRobot(composeTestRule) {
            clickOnSeeDetails()
            checkFullDetails(enrollmentFullDetails)
        }
    }

    @Test
    fun shouldShowIndicatorsDetailsWhenClickOnIndicatorsTab() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            goToAnalytics()
        }

        indicatorsRobot {
            checkDetails("0", "4817")
        }
    }

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
            fillRadioButtonForm(4)
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
            context.getString(R.string.enrollment_single_section_label).replace("%s", "")
        val visitPNCEvent = "PNC Visit"
        val deliveryEvent = "Delivery"
        val visitANCEvent = "ANC Visit (2-4+)"
        val firstANCVisitEvent = "ANC 1st visit"

        setDatePicker()
        prepareTeiToEnrollToOtherProgramAndLaunchActivity(rule)

        teiDashboardRobot(composeTestRule) {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot {
            clickOnAProgramForEnrollment(composeTestRule, womanProgram)
            clickOnAcceptInDatePicker()
            clickOnPersonAttributes(personAttribute)
            waitToDebounce(5000)
            clickOnCalendarItem()
            clickOnAcceptInDatePicker()
            scrollToBottomProgramForm()
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

        indicatorsRobot {
            checkGraphIsRendered(chartName)
        }

        analyticsRobot {
            checkGraphType(1, ChartType.LINE_CHART)
        }
    }

    private fun createExpectedUpperInformation() =
        UpperEnrollmentUIModel(
            "10/1/2021",
            "10/1/2021",
            "Ngelehun CHC"
        )

    private fun createExpectedEnrollmentInformation() =
        EnrollmentUIModel(
            "10/1/2021",
            "10/1/2021",
            "Ngelehun CHC",
            "40.48713205295354",
            "-3.6847423830882633",
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
        const val LAB_MONITORING_SCHEDULE_DATE = "10/9/2019"
    }
}
