package org.dhis2.usescases.event

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.UiDevice
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programevent.robot.programEventsRobot
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    //Flow A — Event Data Entry Form lifecycle.
    @Test
    fun shouldExerciseEventDataEntryFormLifecycle() {
        // ── Setup — create the event via SDK, launch the program list ──────
        val event = createFreshFlowAEvent()
        prepareFlowAProgramEventListAndLaunchActivity(eventListRule)

        programEventsRobot(composeTestRule) {
            clickOnEvent(event.displayDate)
        }

        // ── Step 1 — form renders in editable state ─────────────────────────
        eventRegistrationRobot(composeTestRule) {
            // [ANDROAPP-4647] Save FAB visible when event is editable.
            checkSaveButtonIsDisplayed()

            // [ANDROAPP-1012] Completion-% indicator is visible in the
            // top-right corner of the form's toolbar.
            checkCompletionPercentIsDisplayedInCorner()

            // The form bound to the event correctly — org unit text shows.
            checkOrgUnitIsDisplayed("Ngelehun CHC")

            // [ANDROAPP-4011] DE label rendered using formName, not displayName.
            checkFieldLabelIsFormName(
                formName = "Date of admission",
                displayName = "Admission Date",
            )

            // [ANDROAPP-5266] Legacy "Update" action button is gone.
            checkNoLegacyUpdateActionIsPresent()
        }

        // ── Step 2 — tap sync button → granular sync dialog appears ─────────
        eventRegistrationRobot(composeTestRule) {
            clickSyncButton()
        }
        composeTestRule.waitForIdle()
        // [ANDROAPP-4837] SyncStatusDialog (DialogFragment tag "EVENT_SYNC")
        val syncFragment =
            arrayOfNulls<Any>(1).also {
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    val activity =
                        ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED)
                            .firstOrNull() as? FragmentActivity
                    it[0] =
                        activity?.supportFragmentManager?.findFragmentByTag("EVENT_SYNC")
                }
            }[0] as? androidx.fragment.app.Fragment
        assertNotNull("Expected SyncStatusDialog (EVENT_SYNC) to be attached", syncFragment)
        assertTrue("SyncStatusDialog must be added", syncFragment!!.isAdded)

        // ── Step 3 — press back to dismiss the sync dialog ─────────────────
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeTestRule.waitForIdle()

        // ── Step 4 — try to complete; mandatory empty → blocked ─────────────
        // [ANDROAPP-3832] Tap Save FAB → tap Complete on the resulting bottom sheet
        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnCompleteButton()
        }
        eventRegistrationRobot(composeTestRule) {
            checkSaveButtonIsDisplayed()
        }

        // ── Step 5 — click "No" on the mandatory smoking field ─────────────
        eventRegistrationRobot(composeTestRule) {
            clickNoOnMandatoryField()
        }
        // Let the form's OnSave intent persist the DE value before the
        // next mandatory-check runs at Complete time.
        composeTestRule.waitForIdle()

        // ── Step 6 — complete the event; mandatory now filled → succeeds ───
        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnCompleteButton()
        }
        composeTestRule.waitForIdle()

        // ── Step 7 — list shows the event as completed ─────────────────────
        programEventsRobot(composeTestRule) {
            checkEventIsComplete(event.displayDate)
        }

        // ── Step 8 — re-tap the event from the list ────────────────────────
        programEventsRobot(composeTestRule) {
            clickOnEvent(event.displayDate)
        }

        // ── Step 9 — completed event renders read-only ─────────────────────
        // [ANDROAPP-910] NonEditableReasonBlock with REOPEN_BUTTON is shown and event is read-only
        eventRegistrationRobot(composeTestRule) {
            checkFormIsReadOnly()
        }

        // ── Step 10 — Reopen the event ─────────────────────────────────────
        // [ANDROAPP-2429] Tap REOPEN_BUTTON → event transitions back to ACTIVE
        eventRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnReopen()
            checkEventIsOpen()
        }
        eventRegistrationRobot(composeTestRule) {
            checkSaveButtonIsDisplayed()
        }
    }
}
