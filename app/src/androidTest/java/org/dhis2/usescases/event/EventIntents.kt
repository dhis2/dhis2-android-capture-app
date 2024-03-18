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
const val PROGRAM_XX_TRACKER_UID = "U5KybNCtA3E"
const val EVENT_DETAILS_UID = "oPCuUeDGaIu"
const val EVENT_TO_SHARE_UID = "y0xoVIzBpnL"
const val TEI_EVENT_TO_DELETE_UID = "foc5zag6gbE"
const val ENROLLMENT_EVENT_DELETE_UID = "SolDyMgW3oc"
const val PROGRAM_STAGE_TO_SHARE = "EPEcjy3FWmI"
const val TEI_TO_UPDATE_UID = "LxMVYhJm3Jp"
const val ENROLLMENT_TO_UPDATE_UID = "awZ5RHoJin5"

fun prepareEventDetailsIntentAndLaunchActivity(rule: LazyActivityScenarioRule<EventCaptureActivity>) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        EventCaptureActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, PROGRAM_XX_TRACKER_UID)
        putExtra(EVENT_UID, EVENT_DETAILS_UID)
        putExtra(Constants.EVENT_MODE, EventMode.CHECK)

    }.also { rule.launch(it) }
}

fun prepareEventToDeleteIntentAndLaunchActivity(ruleTeiDashboard: LazyActivityScenarioRule<TeiDashboardMobileActivity>) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        TeiDashboardMobileActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, PROGRAM_TB_UID)
        putExtra(TEI_UID, TEI_EVENT_TO_DELETE_UID)
        putExtra(ENROLLMENT_UID, ENROLLMENT_EVENT_DELETE_UID)
    }.also { ruleTeiDashboard.launch(it) }
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

fun prepareEventToUpdateIntentAndLaunchActivity(ruleTeiDashboard: LazyActivityScenarioRule<TeiDashboardMobileActivity>) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        TeiDashboardMobileActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, PROGRAM_TB_UID)
        putExtra(TEI_UID, TEI_TO_UPDATE_UID)
        putExtra(ENROLLMENT_UID, ENROLLMENT_TO_UPDATE_UID)
    }.also { ruleTeiDashboard.launch(it) }
}
