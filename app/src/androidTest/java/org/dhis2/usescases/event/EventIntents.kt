package org.dhis2.usescases.event

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.commons.Constants
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
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
