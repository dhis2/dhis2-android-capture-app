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

class EventCaptureTest: BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(ProgramEventDetailActivity::class.java, false, false)

    @Test
    fun shouldCreateNewEvent() {
        val eventOrgUnit = "Ngelehun CHC"
        prepareProgramAndLaunchActivity()

        programEventsRobot {
            clickOnAddEvent()
        }

        eventRegistrationRobot {
            clickLocationButton()
            clickNextButton()
            waitToDebounce(600)
        }
        eventRobot {
            clickOnFormFabButton()
        }

        programEventsRobot {
            checkEventWasCreatedAndClosed(eventOrgUnit, 0)
        }

    }
    @Test
    fun shouldOpenExistingEvent() {
        val eventDate = "1/1/2020"
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
        val eventDate = "1/1/2020"
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

    @Test
    fun shouldOpenDetailsOnExistingEvent() {
        val eventDate = "1/1/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity()

        programEventsRobot {
            waitToDebounce(600)
            clickOnEvent(eventDate, eventOrgUnit)
        }

        eventRobot {
            openMenuMoreOptions()
            clickOnDetails()
        }

        eventRegistrationRobot {
        }

    }

    private fun prepareProgramAndLaunchActivity() {
        Intent().apply {
            putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, "q04UBOqq3rp")
        }.also { rule.launchActivity(it) }
    }

    companion object {
        const val NOTE_VALID = "ThisIsJustATest"
    }
}