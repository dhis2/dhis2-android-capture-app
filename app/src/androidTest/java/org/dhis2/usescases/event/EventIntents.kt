package org.dhis2.usescases.event

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.commons.Constants
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

const val EVENT_UID = "EVENT_UID"
const val PROGRAM_UID = "PROGRAM_UID"
const val TEI_UID = "TEI_UID"
const val ENROLLMENT_UID = "ENROLLMENT_UID"
const val PROGRAM_STAGE_UID = "PROGRAM_STAGE_UID"

const val PROGRAM_TB_UID = "ur1Edk5Oe2n"
const val ANTENATAL_CARE_PROGRAM_UID = "lxAQ7Zs9VYR"
const val ANTENATAL_CARE_EVENT_UID = "ohAH6BXIMad"
const val EVENT_TO_SHARE_UID = "y0xoVIzBpnL"
const val TEI_EVENT_TO_DELETE_UID = "foc5zag6gbE"
const val ENROLLMENT_EVENT_DELETE_UID = "SolDyMgW3oc"
const val PROGRAM_STAGE_TO_SHARE = "EPEcjy3FWmI"

// ── Flow A: Event Data Entry Form (ANDROAPP-7620) ────────────────────────────
// Program "XX MAL RDT - Case Registration" — reserved for Flow A.
// See the Confluence Flow Plan for the program configuration applied
// server-side and mirrored into `dhis_test.db`.
const val FLOW_A_PROGRAM_UID = "MoUd5BTQ3lY"
const val FLOW_A_STAGE_UID = "Ge7Eo3FNnbl"

// Seeded events at Ngelehun CHC (DiszpKrYNg8)
const val FLOW_A_EVENT_EMPTY_MANDATORY_UID = "RunQZXYCoUI" // ACTIVE, Diagnosis Method empty, Age=32
const val FLOW_A_EVENT_FILLED_UID = "Hm5qJlu6Fql"          // ACTIVE, all required DEs filled (Microscope, 28, Female)
const val FLOW_A_EVENT_COMPLETED_UID = "mC05P8lXqN7"       // COMPLETED (RDT, 45, Male)

// Flow A data-element UIDs (referenced by tests for field-level assertions)
const val FLOW_A_DE_DIAGNOSIS_METHOD = "lWLkpWMHqEq" // mandatory; formName = "Diagnosis method"
const val FLOW_A_DE_AGE = "vcSXdYGa5St"              // INTEGER; triggers warning rule when > 100

fun prepareFlowAEventEmptyMandatoryAndLaunchActivity(
    rule: LazyActivityScenarioRule<EventCaptureActivity>,
) = launchFlowAEventCaptureActivity(rule, FLOW_A_EVENT_EMPTY_MANDATORY_UID)

fun prepareFlowAEventFilledAndLaunchActivity(
    rule: LazyActivityScenarioRule<EventCaptureActivity>,
) = launchFlowAEventCaptureActivity(rule, FLOW_A_EVENT_FILLED_UID)

fun prepareFlowAEventCompletedAndLaunchActivity(
    rule: LazyActivityScenarioRule<EventCaptureActivity>,
) = launchFlowAEventCaptureActivity(rule, FLOW_A_EVENT_COMPLETED_UID)

private fun launchFlowAEventCaptureActivity(
    rule: LazyActivityScenarioRule<EventCaptureActivity>,
    eventUid: String,
) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        EventCaptureActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, FLOW_A_PROGRAM_UID)
        putExtra(EVENT_UID, eventUid)
        putExtra(Constants.EVENT_MODE, EventMode.CHECK)
    }.also { rule.launch(it) }
}

fun prepareFlowAProgramEventListAndLaunchActivity(
    rule: LazyActivityScenarioRule<ProgramEventDetailActivity>,
) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        ProgramEventDetailActivity::class.java,
    ).apply {
        putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, FLOW_A_PROGRAM_UID)
    }.also { rule.launch(it) }
}

fun prepareEventDetailsIntentAndLaunchActivity(rule: LazyActivityScenarioRule<EventCaptureActivity>) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        EventCaptureActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, ANTENATAL_CARE_PROGRAM_UID)
        putExtra(EVENT_UID, ANTENATAL_CARE_EVENT_UID)
        putExtra(Constants.EVENT_MODE, EventMode.CHECK)

    }.also { rule.launch(it) }
}

fun prepareEventToShareIntentAndLaunchActivity(ruleEventDetail: LazyActivityScenarioRule<EventInitialActivity>) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        EventInitialActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, PROGRAM_TB_UID)
        putExtra(EVENT_UID, EVENT_TO_SHARE_UID)
        putExtra(PROGRAM_STAGE_UID, PROGRAM_STAGE_TO_SHARE)
    }.also { ruleEventDetail.launch(it) }
}
