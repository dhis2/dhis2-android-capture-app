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
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.event.EventCreateProjection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
// The workflow `@Test` creates a fresh event in every run, so the test
// does not depend on hardcoded fixture UIDs that drift across DB
// refreshes. The completed-event sibling test reuses the pre-existing
// Antenatal Care demo event `ohAH6BXIMad`, which is part of the baseline
// demo metadata.
const val FLOW_A_PROGRAM_UID = ANTENATAL_CARE_PROGRAM_UID  // lxAQ7Zs9VYR
const val FLOW_A_STAGE_UID = "dBwrot7S420"                 // Antenatal care visit stage (validationStrategy = ON_COMPLETE)
const val FLOW_A_ORG_UNIT_UID = "DiszpKrYNg8"              // Ngelehun CHC
const val FLOW_A_DEFAULT_COC_UID = "HllvX50cXC0"           // default categoryOptionCombo

// Pre-existing demo event used by the read-only / smoke tests.
const val FLOW_A_EVENT_COMPLETED_UID = ANTENATAL_CARE_EVENT_UID // ohAH6BXIMad — COMPLETED

/**
 * Creates a fresh ACTIVE event in the local SDK database (Antenatal Care
 * program, Ngelehun CHC, today's date, no data values — so the mandatory
 * DE is empty) and launches `EventCaptureActivity` in CHECK mode for it.
 *
 * Returns the UID of the newly-created event so the test can reference it
 * if needed.
 */
fun createFreshFlowAEventAndLaunchActivity(
    rule: LazyActivityScenarioRule<EventCaptureActivity>,
): String {
    val event = createFreshFlowAEvent()
    Intent(
        ApplicationProvider.getApplicationContext(),
        EventCaptureActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, FLOW_A_PROGRAM_UID)
        putExtra(EVENT_UID, event.uid)
        putExtra(Constants.EVENT_MODE, EventMode.CHECK)
    }.also { rule.launch(it) }
    return event.uid
}

/**
 * One fresh Flow A event, ready for a workflow test that enters via the
 * program event list. Returns both the UID and the display date string
 * (`dd/MM/yyyy`) so the test can locate the row in the list and re-tap
 * it later in the journey.
 */
data class FreshFlowAEvent(
    val uid: String,
    val displayDate: String,
)

fun createFreshFlowAEvent(): FreshFlowAEvent {
    val d2 = D2Manager.getD2()
    val now = Date()
    val uid =
        d2.eventModule().events().blockingAdd(
            EventCreateProjection
                .builder()
                .program(FLOW_A_PROGRAM_UID)
                .programStage(FLOW_A_STAGE_UID)
                .organisationUnit(FLOW_A_ORG_UNIT_UID)
                .attributeOptionCombo(FLOW_A_DEFAULT_COC_UID)
                .build(),
        )
    d2.eventModule().events().uid(uid).setEventDate(now)
    val displayDate = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(now)
    return FreshFlowAEvent(uid, displayDate)
}

fun prepareFlowAEventCompletedAndLaunchActivity(
    rule: LazyActivityScenarioRule<EventCaptureActivity>,
) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        EventCaptureActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, FLOW_A_PROGRAM_UID)
        putExtra(EVENT_UID, FLOW_A_EVENT_COMPLETED_UID)
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
