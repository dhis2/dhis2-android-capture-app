package org.dhis2.usescases.event

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.robot.eventRobot
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

    // ── Flow A — Event Data Entry Form lifecycle (ANDROAPP-7620) ─────────────
    //
    // Single workflow test that walks through the data-entry form lifecycle
    // of an ACTIVE event in program MoUd5BTQ3lY ("XX MAL RDT - Case
    // Registration"). Each step asserts one or more Zephyr cases as
    // checkpoints inside the same user journey. The Zephyr case ID stays
    // next to the assertion that proves it.
    //
    // Server config (mirrored into dhis_test.db):
    //  • DE `lWLkpWMHqEq` (Diagnosis Method) is mandatory, has formName
    //    "Diagnosis method" (≠ displayName).
    //  • Program rule "Warn when age > 100" (RiLiRwFUb7l) fires a SHOWWARNING
    //    action on DE `vcSXdYGa5St` (Age).
    //  • ProgramStage validationStrategy = ON_COMPLETE.
    //
    // Fixture event: `RunQZXYCoUI` — ACTIVE, mandatory DE empty, Age = 32.
    // See EventIntents.kt for all seeded event UIDs.
    //
    // Flow plan: https://dhis2.atlassian.net/wiki/spaces/MOB/pages/1792999427

    /**
     * Walks through the form lifecycle for an active event in the reserved
     * Flow A program. Sequential checkpoints inside one journey:
     *
     *  1. Open the active event with an empty mandatory DE; verify the form
     *     renders correctly:
     *      - Save FAB is visible             [ANDROAPP-4647]
     *      - DE label uses formName          [ANDROAPP-4011]
     *      - no legacy "Update" action       [ANDROAPP-5266]
     *  2. Tap Save FAB → tap Complete → completion blocked because the
     *     mandatory DE is empty             [ANDROAPP-3832]
     *
     * Three further Zephyr cases (ANDROAPP-910, 2429, 6405) are documented in
     * the flow plan but deferred until their fixtures land — see the
     * "Deferred" block at the bottom of this method.
     */
    @Test
    fun shouldExerciseEventDataEntryFormLifecycle() {
        // ── Setup — launch the active, empty-mandatory event ────────────────
        prepareFlowAEventEmptyMandatoryAndLaunchActivity(rule)

        // ── Step 1 — form renders in editable state ─────────────────────────
        eventRegistrationRobot(composeTestRule) {
            // [ANDROAPP-4647] Save FAB visible when event is editable.
            checkSaveButtonIsDisplayed()

            // [ANDROAPP-4011] DE label rendered using formName, not displayName.
            checkFieldLabelIsFormName(
                formName = "Diagnosis method",
                displayName = "XX MAL RDT TRK - Diagnosis Method",
            )

            // [ANDROAPP-5266] Legacy "Update" action button is gone — replaced
            // by auto-save. No node with the text "Update" should render in
            // the form.
            checkNoLegacyUpdateActionIsPresent()
        }

        // ── Step 2 — try to complete with mandatory DE empty ────────────────
        // [ANDROAPP-3832] Mandatory DEs block completion.
        // Tap the Save FAB → tap Complete on the resulting bottom sheet.
        // Form must remain open because the mandatory Diagnosis Method DE is
        // still empty.
        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnCompleteButton()
        }
        eventRegistrationRobot(composeTestRule) {
            checkSaveButtonIsDisplayed()
        }

        // ── Deferred checkpoints — pending fixtures (see flow plan) ─────────
        //
        // [ANDROAPP-910]  Completed-event form is read-only. The SDK currently
        //                 classifies completed events as still editable for the
        //                 admin test user (who holds F_UNCOMPLETE_EVENT), so
        //                 NonEditableReasonBlock / REOPEN_BUTTON never render.
        //                 Needs a non-admin seeded user or a different SDK probe.
        //
        // [ANDROAPP-2429] Re-open completed event requires F_UNCOMPLETE_EVENT.
        //                 Needs two seeded users + a helper to switch between
        //                 them inside a single test.
        //
        // [ANDROAPP-6405] Discard / Review-warnings sheet on complete-with-
        //                 warnings. The warning rule is wired (Age > 100) but
        //                 no seeded event triggers it; needs a Robot helper to
        //                 change a DE value in the running form, or a fourth
        //                 seed event with Age > 100.
    }
}
