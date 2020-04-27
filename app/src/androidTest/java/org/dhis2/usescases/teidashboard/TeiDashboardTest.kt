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

        prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule)

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
        prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule)

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
        prepareTeiWithExistingNoteIntentAndLaunchActivity(rule)

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
        prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuReOpen()
            checkUnlockIconIsDisplay()
            checkCanAddEvent()
        }
    }

    @Test
    fun shouldDeactivateTEIWhenClickOpen() {
        prepareTeiOpenedProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuDeactivate()
            checkLockIconIsDisplay()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldCompleteTEIWhenClickOpen() {
        prepareTeiOpenedForCompleteProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkLockCompleteIconIsDisplay()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldShowQRWhenClickOnShare() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnShareButton()
            clickOnNextQR()
        }
    }

    @Test
    fun shouldMakeAReferral() {
        prepareTeiOpenedForReferralProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            clickOnFirstReferralEvent()
            clickOnReferralOption()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated("Lab monitoring")
        }
    }

    @Test
    fun shouldSuccessfullyScheduleAnEvent() {
        prepareTeiOpenedWithNoPreviousEventProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnScheduleNew()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated("Lab monitoring")
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

        prepareTeiOpenedWithNoPreviousEventProgrammeIntentAndLaunchActivity(rule)

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
    fun shouldNotBeAbleToCreateNewEventsWhenFull() {
        prepareTeiOpenedWithFullEvents(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            checkCannotAddMoreEventToastIsShown()
        }
    }

    @Test
    fun shouldOpenEventAndSaveSuccessfully(){
        prepareTeiOpenedProgrammeIntentAndLaunchActivity(rule)

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
    @Ignore
    fun shouldSuccessfullyCreateRelationshipWhenClickAdd() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule)

    //    Thread.sleep(10000)
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
        //prepareTeiToDelete()
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

    @Test
    fun shouldShowCorrectInfoWhenOpenTEI() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            checkUpperInfo("2021-01-10", "2021-01-10", "Ngelehun CHC")
        }
    }

    @Test
    fun shouldShowTEIDetailsWhenClickOnSeeDetails() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnSeeDetails()
            checkFullDetails("2021-01-10", "2021-01-10", "Ngelehun CHC", "40.48713205295354", "-3.6847423830882633", "Filona", "Ryder", "Female")
        }
    }

    @Test
    fun shouldShowIndicatorsDetailsWhenClickOnIndicatorsTab() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnIndicatorsTab()
        }

        indicatorsRobot {
            checkDetails("0", "4817")
        }
    }

    companion object{
        const val TOAST_TEXT_SAVED = "Note saved"
        const val NOTE_VALID = "ThisIsJustATest"
        const val NOTE_INVALID = "InvalidNote"
        const val NOTE_EXISTING_TEXT = "This is a note test"
        const val USER = "android"

        const val API_TEI_1_RESPONSE_OK = "mocks/teilist/teilist_1.json"
        const val API_TEI_2_RESPONSE_OK = "mocks/teilist/teilist_2.json"
        const val API_TEI_3_RESPONSE_OK = "mocks/teilist/teilist_3.json"
    }
}