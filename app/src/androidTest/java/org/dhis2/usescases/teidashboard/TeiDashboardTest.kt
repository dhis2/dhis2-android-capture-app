package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.entity.EnrollmentUIModel
import org.dhis2.usescases.teidashboard.entity.UpperEnrollmentUIModel
import org.dhis2.usescases.teidashboard.robot.*
import org.hisp.dhis.android.core.mockwebserver.ResponseController.GET
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

    @Test
    fun shouldSuccessfullyCreateANoteWhenClickCreateNote() {
        setupCredentials()

        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnNotesTab()
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
            clickOnNotesTab()
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
            clickOnNotesTab()
        }

        noteRobot {
            clickOnNoteWithPosition(0)
            checkNoteDetails("@${USER}", NOTE_EXISTING_TEXT)
        }
    }

    @Test
    fun shouldReactivateTEIWhenClickReOpen() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuReOpen()
            checkUnlockIconIsDisplay()
            checkCanAddEvent()
        }
    }

    @Test
    fun shouldDeactivateTEIWhenClickOpen() {
        prepareTeiOpenedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuDeactivate()
            checkLockIconIsDisplay()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldCompleteTEIWhenClickOpen() {
        prepareTeiOpenedForCompleteProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkLockCompleteIconIsDisplay()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldShowQRWhenClickOnShare() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnShareButton()
            clickOnNextQR()
        }
    }

    @Test
    fun shouldMakeAReferral() {
        prepareTeiOpenedForReferralProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            clickOnFirstReferralEvent()
            clickOnReferralOption()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated(LAB_MONITORING)
        }
    }

    @Test
    fun shouldSuccessfullyScheduleAnEvent() {
        prepareTeiOpenedWithNoPreviousEventProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnScheduleNew()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated(LAB_MONITORING)
        }
    }

    @Test
    fun shouldNotBeAbleToCreateNewEventsWhenFull() {
        prepareTeiOpenedWithFullEventsAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            checkCannotAddMoreEventToastIsShown()
        }
    }

    @Test
    fun shouldOpenEventAndSaveSuccessfully(){
        setupCredentials()

        prepareTeiOpenedProgrammeAndLaunchActivity(rule)

        val babyPostNatal = 0
        teiDashboardRobot {
            clickOnEventWithPosition(babyPostNatal)
        }

        eventRobot {
            scrollToBottomFormulary()
            clickOnFormularyFabButton()
            clickOnFinish()
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
            clickOnIndicatorsTab()
        }

        indicatorsRobot {
            checkDetails("0", "4817")
        }
    }

    @Test
    fun shouldSuccessfullyCreateANewEvent() {
        prepareTeiToCreateANewEventAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnCreateNewEvent()
            clickOnFirstReferralEvent()
            /*clickOnSecondStageEvent()
            clickOnReferralNextButton()
            Thread.sleep(1000)
            clickOnNumberField()
            Thread.sleep(1000)
            typeNumber("1") //90 percent of the view
            Thread.sleep(1000)
            clickOnEventFab()*/
            clickOnReferralNextButton()
            Thread.sleep(1000)
            clickOnRadioButtonForm(0) //not sure why is not clicking but in case it's clicking, yes is not selected
            Thread.sleep(2500)
            clickOnRadioButtonForm(1)
            clickOnRadioButtonForm(2)
            // click on finish
        }
    }

    @Test
    @Ignore
    fun shouldEnrollToOtherProgramWhenClickOnProgramEnrollments() {
        //launch tei
        //click on more options
        // click on Program enrollments
        // choose a Program to be enroll
        // choose date and accept
        // click on save in enrollment
        // if Child Program after save will ask to type AGAR then to choose option (Finish or Finish and Complete)

        prepareTeiOpenedWithNoPreviousEventProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
            clickOnAProgramForEnrollment() // is not clicking on enrollment btn
            //clickOnAcceptEnrollmentDate()
            //clickOnSaveEnrollment()
            Thread.sleep(1000)
        }
    }

    @Test
    @Ignore
    fun shouldSuccessfullyCreateRelationshipWhenClickAdd() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnRelationshipTab()
        }

        relationshipRobot {
            clickOnFabAdd()
            clickOnRelationshipType()
            // click on a TEI
            // check relationship was created
            Thread.sleep(5000)
            //    clickOnMotherRelationship()
        }
    }

    @Test
    @Ignore
    fun shouldDeleteTeiSuccessfully() {
        // open more options
        // click on delete tei
        // check tei was deleted and not show on reclycler view
        mockWebServerRobot.addResponse(GET, "/api/trackedEntityInstances?.*", API_TEI_1_RESPONSE_OK)
        mockWebServerRobot.addResponse(GET, "/api/trackedEntityInstances?.*", API_TEI_2_RESPONSE_OK)
        mockWebServerRobot.addResponse(GET, "/api/trackedEntityInstances?.*", API_TEI_3_RESPONSE_OK)
        //https://play.dhis2.org/android-current/api/trackedEntityInstances/query?ou=DiszpKrYNg8&ouMode=DESCENDANTS&program=IpHINAT79UW&paging=true&page=1&pageSize=10
        setupCredentials()
        //prepareTeiToDeleteAndLaunchActivity()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        teiDashboardRobot {
            /*clickOnMenu()
            clickOnMenuDeleteTEI()
            checkTEIIsDelete()*/
            onView(withId(R.id.close_filter)).perform(click())
            clickOnTEI()
            // rule.getactivity == null assert
            Thread.sleep(10000)
        }
    }

    private fun createExpectedUpperInformation() = UpperEnrollmentUIModel("2021-01-10", "2021-01-10", "Ngelehun CHC")
    private fun createExpectedEnrollmentInformation() = EnrollmentUIModel("2021-01-10", "2021-01-10", "Ngelehun CHC", "40.48713205295354", "-3.6847423830882633", "Filona", "Ryder", "Female")

    companion object{
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