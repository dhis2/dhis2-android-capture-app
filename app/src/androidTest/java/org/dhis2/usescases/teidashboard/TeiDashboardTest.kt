package org.dhis2.usescases.teidashboard

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeiDashboardTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    override fun setUp() {
        super.setUp()
        //init robot
    }

    @Test
    fun shouldSuccessfullyCreateANoteWhenClickCreateNote() {
        setupCredentials()
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnPinTab()
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
            clickOnPinTab()
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
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnPinTab()
        }

        noteRobot {
            clickOnNote(0)
            checkNoteDetails("@${USER}", NOTE_VALID)
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
            // check you can add event
        }
        Thread.sleep(10000)
    }

    @Test
    fun shouldDeactivateTEIWhenClickOpen() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()
        //should be a teiOpened

        teiDashboardRobot {
            clickOnMenu()
            clickOnMenuOpen()
            checkLockIconIsDisplay()
            checkCanNotAddEvent()
            // check you can't add event
        }
        Thread.sleep(10000)
    }

    @Test
    fun shouldShowQRWhenClickOnShare() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnShareButton()
            clickOnNextQR()
            //click on next
        }
    }

    private fun prepareTeiCompletedProgrammeIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE)
        }.also { rule.launchActivity(it) }
    }

    private fun prepareTeiOpenedProgrammeIntentAndLaunchActivity() {

    }

    companion object{
        const val CHILD_PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val TEI_UID = "TEI_UID"
        const val TEI_UID_VALUE = "vOxUH373fy5"

        const val TOAST_TEXT_SAVED = "Note saved"
        const val NOTE_VALID = "ThisIsJustATest"
        const val NOTE_INVALID = "InvalidNote"
        const val USER = "android"
    }
}