package org.dhis2.usescases.event

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.event.EventCreateProjection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val ANTENATAL_CARE_PROGRAM_UID = "lxAQ7Zs9VYR"

// ── Flow 1: Event Data Entry Form (ANDROAPP-7620) ────────────────────────────
// The workflow `@Test` creates a fresh event in every run, so the test
// does not depend on hardcoded fixture UIDs that drift across DB
// refreshes.
const val FLOW_A_PROGRAM_UID = ANTENATAL_CARE_PROGRAM_UID  // lxAQ7Zs9VYR
const val FLOW_A_STAGE_UID = "dBwrot7S420"                 // Antenatal care visit stage (validationStrategy = ON_COMPLETE)
const val FLOW_A_ORG_UNIT_UID = "DiszpKrYNg8"              // Ngelehun CHC
const val FLOW_A_DEFAULT_COC_UID = "HllvX50cXC0"           // default categoryOptionCombo

// ── Flow 2: create-form mandatory block (ANDROAPP-7728) ──────────────────────
const val FLOW_D_PROGRAM_UID = "bMcwwoVnbSR"
const val FLOW_D_ORG_UNIT_NAME = "Ngelehun CHC"
const val FLOW_D_CATEGORY_NAME = "Implementing Partner"
const val FLOW_D_VISIT_DATE_LABEL = "Visit date"

const val FLOW_D_GENDER_LABEL = "Gender"
const val FLOW_D_RDT_LABEL = "RDT test result"
const val FLOW_D_TREATMENT_LABEL = "Treatment"

fun todayDisplayDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())

/**
 * One fresh Flow 1 event, ready for a workflow test that enters via the
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

fun prepareFlowDProgramEventListAndLaunchActivity(
    rule: LazyActivityScenarioRule<ProgramEventDetailActivity>,
) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        ProgramEventDetailActivity::class.java,
    ).apply {
        putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, FLOW_D_PROGRAM_UID)
    }.also { rule.launch(it) }
}
