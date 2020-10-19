package org.dhis2.usescases.programevent

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.event.eventRegistrationRobot
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programevent.robot.programEventsRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.noteRobot
import org.junit.Rule
import org.junit.Test

class ProgramEventTest: BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(ProgramEventDetailActivity::class.java, false, false)

    @Test
    fun shouldCreateNewEventAndCompleteIt() {
        val eventOrgUnit = "Ngelehun CHC"
        prepareProgramAndLaunchActivity()

        programEventsRobot {
            clickOnAddEvent()
        }

        eventRegistrationRobot {
            clickNextButton()
            waitToDebounce(600)
        }
        eventRobot {
            clickOnFormFabButton()
            clickOnFinishAndComplete()
        }

        programEventsRobot {
            checkEventWasCreatedAndClosed(eventOrgUnit, 0)
        }

    }
    @Test
    fun shouldOpenExistingEvent() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity()

        programEventsRobot {
            waitToDebounce(600)
            clickOnEvent(eventDate, eventOrgUnit)
        }

        eventRobot {
            checkDetails(eventDate, eventOrgUnit)
        }
    }

    @Test
    fun shouldAddNoteOnExistingEvent() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity()

        programEventsRobot {
            waitToDebounce(600)
            clickOnEvent(eventDate, eventOrgUnit)
        }

        eventRobot {
            clickOnNotesTab()
        }

        noteRobot {
            clickOnFabAddNewNote()
            typeNote(NOTE_VALID)
            clickOnSaveButton()
            checkNewNoteWasCreated(NOTE_VALID)
        }
    }

    private fun prepareProgramAndLaunchActivity() {
        Intent().apply {
            putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, "lxAQ7Zs9VYR")
        }.also { rule.launchActivity(it) }
    }

    companion object {
        const val NOTE_VALID = "ThisIsJustATest"
    }
}