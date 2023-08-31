package org.dhis2.usescases.teidashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.robot.searchTeiRobot
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
    val rule = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createComposeRule()
    @Test
    fun shouldSuccessfullyCreateANoteWhenClickCreateNote() {
        setupCredentials()

        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
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

        teiDashboardRobot {
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

        teiDashboardRobot {
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

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuReOpen()
            checkUnlockIconIsDisplay()
            checkAllEventsCompleted(1)
        }
    }

    @Test
    fun shouldShowInactiveProgramWhenClickDeactivate() {
        prepareTeiOpenedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuDeactivate()
            checkLockIconIsDisplay()
            checkCanNotAddEvent()
            checkAllEventsAreInactive(1)
        }
    }

    @Test
    fun shouldCompleteProgramWhenClickComplete() {
        prepareTeiOpenedForCompleteProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkLockCompleteIconIsDisplay()
            checkCanNotAddEvent()
            checkAllEventsAreClosed(1)
        }
    }

    @Test
    fun shouldShowQRWhenClickOnShare() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnShareButton()
            clickOnNextQR()
        }
    }

    @Test
    fun shouldMakeAReferral() {
        prepareTeiOpenedForReferralProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnFab()
            clickOnReferral()
            clickOnFirstReferralEvent()
            clickOnReferralOption()
            clickOnReferralNextButton()
            checkEventWasCreated(LAB_MONITORING)
        }
    }

    @Test
    fun shouldSuccessfullyScheduleAnEvent() {
        prepareTeiOpenedWithNoPreviousEventProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnFab()
            clickOnScheduleNew()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventWasCreated(LAB_MONITORING)
        }
    }

    @Test
    fun shouldNotBeAbleToCreateNewEventsWhenFull() {
        prepareTeiOpenedWithFullEventsAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldOpenEventAndSaveSuccessfully() {
        setupCredentials()

        prepareTeiOpenedProgrammeAndLaunchActivity(rule)

        val babyPostNatal = 0
        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnEventWithPosition(babyPostNatal)
        }

        eventRobot {
            scrollToBottomForm()
            clickOnFormFabButton()
            clickOnNotNow(composeTestRule)
        }
    }

    @Test
    fun shouldShowCorrectInfoWhenOpenTEI() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        val upperInformation = createExpectedUpperInformation()

        teiDashboardRobot {
            checkUpperInfo(upperInformation)
        }
    }

    @Test
    fun shouldShowTEIDetailsWhenClickOnSeeDetails() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        val enrollmentFullDetails = createExpectedEnrollmentInformation()

        teiDashboardRobot {
            clickOnSeeDetails()
            checkFullDetails(enrollmentFullDetails)
        }
    }

    @Test
    fun shouldShowIndicatorsDetailsWhenClickOnIndicatorsTab() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            goToAnalytics()
        }

        indicatorsRobot {
            checkDetails("0", "4817")
        }
    }

    @Test
    fun shouldSuccessfullyCreateANewEvent() {
        prepareTeiToCreateANewEventAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnFab()
            clickOnCreateNewEvent()
            clickOnFirstReferralEvent()
            waitToDebounce(2000)
            clickOnReferralNextButton()
            waitToDebounce(600)
        }

        eventRobot {
            fillRadioButtonForm(4)
            clickOnFormFabButton()
            clickOnNotNow(composeTestRule)
        }

        teiDashboardRobot {
            checkEventWasCreatedAndOpen(LAB_MONITORING, 0)
        }
    }

    @Test
    fun shouldOpenEventEditAndSaveSuccessfully() {
        prepareTeiOpenedToEditAndLaunchActivity(rule)

        val labMonitoring = 2

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnEventWithPosition(labMonitoring)
            waitToDebounce(600)
        }

        eventRobot {
            waitToDebounce(600)
            fillRadioButtonForm(4)
            clickOnFormFabButton()
            clickOnCompleteButton(composeTestRule)
            waitToDebounce(600)
        }

        teiDashboardRobot {
            checkEventWasCreatedAndClosed(LAB_MONITORING, 2)
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

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnTimelineEvents()
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot {
            clickOnAProgramForEnrollment(composeTestRule, womanProgram)
            clickOnAcceptEnrollmentDate()
            clickOnPersonAttributes(personAttribute)
            waitToDebounce(5000)
            clickOnCalendarItem()
            clickOnAcceptEnrollmentDate()
            scrollToBottomProgramForm()
            clickOnSaveEnrollment()
        }

        teiDashboardRobot {
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

        teiDashboardRobot {
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

        const val API_TEI_1_RESPONSE_OK = "mocks/teilist/teilist_1.json"
        const val API_TEI_2_RESPONSE_OK = "mocks/teilist/teilist_2.json"
        const val API_TEI_3_RESPONSE_OK = "mocks/teilist/teilist_3.json"
    }
}
