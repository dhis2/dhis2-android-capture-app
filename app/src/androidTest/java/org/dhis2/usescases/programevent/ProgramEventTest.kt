package org.dhis2.usescases.programevent

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.ActivityTestRule
import org.dhis2.AppTest.Companion.DB_TO_IMPORT
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.event.eventRegistrationRobot
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programEventDetail.eventList.EventListFragment
import org.dhis2.usescases.programevent.robot.programEventsRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.junit.Rule
import org.junit.Test

class ProgramEventTest : BaseTest() {

    private val atenatalCare = "lxAQ7Zs9VYR"
    private val informationCampaign = "q04UBOqq3rp"

    @get:Rule
    val rule = ActivityTestRule(ProgramEventDetailActivity::class.java, false, false)

    @get:Rule
    val composeTestRule = createComposeRule()

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @Test
    fun shouldCreateNewEventAndCompleteIt() {
        val eventOrgUnit = "Ngelehun CHC"
        prepareProgramAndLaunchActivity(atenatalCare)
        disableRecyclerViewAnimations()

        programEventsRobot {
            clickOnAddEvent()
        }
        eventRegistrationRobot {
            clickNextButton()
        }
        eventRobot {
            clickOnFormFabButton()
            clickOnCompleteButton(composeTestRule)
        }
        programEventsRobot {
            checkEventWasCreatedAndClosed(eventOrgUnit, 0)
        }
    }

    private fun disableRecyclerViewAnimations() {
        val activity = rule.activity
        activity.runOnUiThread {
            activity.supportFragmentManager.findFragmentByTag("EVENT_LIST").apply {
                (this as EventListFragment).binding.recycler.itemAnimator = null
            }
        }
    }

    @Test
    fun shouldOpenExistingEvent() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(atenatalCare)

        programEventsRobot {
            clickOnEvent(eventDate, eventOrgUnit)
        }

        eventRobot {
            checkDetails(eventDate, eventOrgUnit)
        }
    }

    @Test
    fun shouldCompleteAnEventAndReopenIt() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(atenatalCare)
        disableRecyclerViewAnimations()

        programEventsRobot {
            clickOnEvent(eventDate, eventOrgUnit)
        }

        eventRobot {
            clickOnFormFabButton()
            clickOnCompleteButton(composeTestRule)
            waitToDebounce(400)
        }

        programEventsRobot {
            checkEventIsComplete(eventDate, eventOrgUnit)
            clickOnEvent(eventDate, eventOrgUnit)
        }

        eventRobot {
            clickOnDetails()
            clickOnReopen()
            pressBack()
        }

        programEventsRobot {
            waitToDebounce(800)
            checkEventIsOpen(eventDate, eventOrgUnit)
        }
    }

    @Test
    fun shouldOpenDetailsOfExistingEvent() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(atenatalCare)

        programEventsRobot {
            clickOnEvent(eventDate, eventOrgUnit)
        }
        eventRobot {
            clickOnDetails()
            checkEventDetails(eventDate, eventOrgUnit, composeTestRule)
        }
    }

    @Test
    fun shouldDeleteEvent() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(atenatalCare)
        disableRecyclerViewAnimations()

        programEventsRobot {
            clickOnEvent(eventDate, eventOrgUnit)
        }
        eventRobot {
            openMenuMoreOptions()
            clickOnDelete()
            clickOnDeleteDialog()
        }
        programEventsRobot {
            checkEventWasDeleted(eventDate, eventOrgUnit)
        }
        rule.activity.application.deleteDatabase(DB_TO_IMPORT)
    }

    @Test
    fun shouldOpenEventAndShowMap() {

        prepareProgramAndLaunchActivity(informationCampaign)

        programEventsRobot {
            clickOnMap()
            checkMapIsDisplayed()
        }
    }

    private fun prepareProgramAndLaunchActivity(programUid: String) {
        Intent().apply {
            putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, programUid)
        }.also { rule.launchActivity(it) }
    }
}
