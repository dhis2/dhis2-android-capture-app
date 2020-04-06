package org.dhis2.usescases.teidashboard

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
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
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnNotesTab()
        }

        noteRobot {
            checkFabDisplay()
            clickOnFabAddNewNote()
            typeNote(NOTE_VALID)
            clickOnSaveButton()
            checkToastDisplayed(TOAST_TEXT_SAVED)
            checkNewNoteWasCreated(NOTE_VALID)
        }
    }

    @Test
    fun shouldNotCreateANoteWhenClickClear() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnNotesTab()
        }

        noteRobot {
            checkFabDisplay()
            clickOnFabAddNewNote()
            typeNote(NOTE_INVALID)
            clickOnClearButton()
            clickYesOnAlertDialog()
            checkNoteWasNotCreated(NOTE_INVALID)
        }
    }

    @Test
    fun shouldOpenNotesDetailsWhenClickOnNote() {
        prepareTeiWithExistingNoteIntentAndLaunchActivity()

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
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnMenu()
            clickOnMenuReOpen()
            checkUnlockIconIsDisplay()
            checkCanAddEvent()
        }
    }

    @Test
    fun shouldDeactivateTEIWhenClickOpen() {
        prepareTeiOpenedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnMenu()
            clickOnMenuDeactivate()
            checkLockIconIsDisplay()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldCompleteTEIWhenClickOpen() {
        prepareTeiOpenedForCompleteProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnMenu()
            clickOnMenuComplete()
            checkLockCompleteIconIsDisplay()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldShowQRWhenClickOnShare() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnShareButton()
            clickOnNextQR()
            //Use a custom matcher to iterate loop
        }
    }

    @Test
    fun shouldBeAbleToMakeAReferral() {
        prepareTeiOpenedForReferralProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            clickOnFirstReferralEvent()
            checkEventIsCreated("EventName")
        }
    }

    @Test
    fun shouldNotBeAbleToCreateNewEventsWhenFull() {
        prepareTeiOpenedWithFullEvents()
        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            checkCannotAddMoreEventToasIsShown()
        }
    }

    @Test
    fun shouldOpenEventAndSaveSuccessfully(){
        prepareTeiOpenedProgrammeIntentAndLaunchActivity()

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
    fun shouldSuccessfullyCreateRelationshipWhenClickAdd() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        Thread.sleep(10000)
        teiDashboardRobot {
            clickOnRelationshipTab()
        }

        relationshipRobot {
            clickOnFabAdd()
            // click on a relationship type ?
            // click on a TEI
            // check relationship was created
            Thread.sleep(5000)
            clickOnMotherRelationship()
        }
    }

    @Test
    fun shouldDeleteTeiSuccessfully() {
        // open more options
        // click on delete tei
        // check tei was deleted and not show on reclycler view
        setupCredentials()
        //prepareTeiToDelete()
        prepareChildProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            /*clickOnMenu()
            clickOnMenuDeleteTEI()
            checkTEIIsDelete()*/
            clickOnTEI()
            // rule.getactivity == null assert
            Thread.sleep(5000)
        }
    }

    private fun prepareTeiCompletedProgrammeIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE_COMPLETED)
        }.also { rule.launchActivity(it) }
    }

    private fun prepareTeiOpenedForReferralProgrammeIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE_OPEN_REFERRAL)
        }.also { rule.launchActivity(it) }
    }

    private fun prepareTeiOpenedProgrammeIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE_OPENED)
        }.also { rule.launchActivity(it) }
    }

    private fun prepareTeiOpenedForCompleteProgrammeIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE_OPEN_TO_COMPLETE)
        }.also { rule.launchActivity(it) }
    }

    private fun prepareTeiWithExistingNoteIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE_WITH_NOTE)
        }.also { rule.launchActivity(it) }
    }

    private fun prepareTeiOpenedWithFullEvents(){
        Intent().apply{
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE_OPENED_FULL)
        }.also { rule.launchActivity(it) }
    }

    private fun prepareChildProgrammeIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(SearchTETest.CHILD_PROGRAM_UID, SearchTETest.CHILD_PROGRAM_UID_VALUE)
            putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
        }.also { ruleSearch.launchActivity(it) }
    }

    private fun prepareTeiToDelete(){
        Intent().apply{
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE_TO_DELETE)
        }.also { rule.launchActivity(it) }
    }

    companion object{
        const val CHILD_PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val TEI_UID = "TEI_UID"
        const val TEI_UID_VALUE_COMPLETED = "vOxUH373fy5"
        const val TEI_UID_VALUE_OPENED = "Pqv3LrNECkn"
        const val TEI_UID_VALUE_OPENED_FULL = "r2FEXpX6ize"
        const val TEI_UID_VALUE_OPEN_REFERRAL = "PQfMcpmXeFE"
        const val TEI_UID_VALUE_OPEN_TO_COMPLETE = "qx4yw1EuxmW"
        const val TEI_UID_VALUE_WITH_NOTE = "UtDZmrX5lSd"
        const val TEI_UID_VALUE_TO_DELETE = "SHnmavBQu72"

        const val TOAST_TEXT_SAVED = "Note saved"
        const val NOTE_VALID = "ThisIsJustATest"
        const val NOTE_INVALID = "InvalidNote"
        const val NOTE_EXISTING_TEXT = "This is a note test"
        const val USER = "android"
    }
}