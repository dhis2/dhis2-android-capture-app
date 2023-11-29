package org.dhis2.usescases.form

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.eventswithoutregistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

const val EVENT_UID = "EVENT_UID"
const val PROGRAM_UID = "PROGRAM_UID"
const val TEI_UID = "TEI_UID"
const val ENROLLMENT_UID = "ENROLLMENT_UID"
const val PROGRAM_STAGE_UID = "PROGRAM_STAGE_UID"
const val TE_UID = "TRACKED_ENTITY_UID"

const val PROGRAM_XX_PROGRAM_RULES = "jIT6KcSZiAN"
const val EVENT_GAMMA = "lwwdP4k1SnI"
const val PROGRAM_STAGE_GAMMA = "wtf7lKvaoY8"
const val TEI_BLA = "bxEs3ZwGcZg"
const val TRACKED_ENTITY_TYPE = "nEenWmSyUEp"

fun prepareEventProgramRuleIntentAndLaunchActivity(rule: ActivityTestRule<EventCaptureActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, PROGRAM_XX_PROGRAM_RULES)
        putExtra(EVENT_UID, EVENT_GAMMA)
    }.also { rule.launchActivity(it) }
}

fun prepareTEIIntentAndLaunchActivity(ruleTeiDashboard: ActivityTestRule<TeiDashboardMobileActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, PROGRAM_XX_PROGRAM_RULES)
        putExtra(TEI_UID, TEI_BLA)
    }.also { ruleTeiDashboard.launchActivity(it) }
}

fun startSearchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, PROGRAM_XX_PROGRAM_RULES)
        putExtra(TE_UID, TRACKED_ENTITY_TYPE)
    }.also { ruleSearch.launchActivity(it) }
}