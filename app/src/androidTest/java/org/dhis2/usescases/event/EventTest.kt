package org.dhis2.usescases.event

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTest : BaseTest() {

    @get:Rule
    val rule = lazyActivityScenarioRule<EventCaptureActivity>(launchActivity = false)

    @get:Rule
    val ruleTeiDashboard =
        lazyActivityScenarioRule<TeiDashboardMobileActivity>(launchActivity = false)

    @get:Rule
    val ruleEventDetail = lazyActivityScenarioRule<EventInitialActivity>(launchActivity = false)

    @get:Rule
    val eventListRule = lazyActivityScenarioRule<ProgramEventDetailActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldShowEventDetailsWhenClickOnDetailsInsideSpecificEvent() {
        val completion = 100
        val orgUnit = "Ngelehun CHC"

        prepareEventDetailsIntentAndLaunchActivity(rule)

        eventRegistrationRobot(composeTestRule) {
            checkEventDataEntryIsOpened(completion, orgUnit)
        }
    }
}