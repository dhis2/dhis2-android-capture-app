package org.dhis2.usescases.teidashboard

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

private const val PROGRAM_UID = "PROGRAM_UID"
private const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"
private const val TB_PROGRAM_UID = "ur1Edk5Oe2n"
private const val TEI_UID = "TEI_UID"
private const val TEI_UID_VALUE_COMPLETED = "vOxUH373fy5"
private const val TEI_UID_VALUE_OPENED = "Pqv3LrNECkn"
private const val TEI_UID_VALUE_OPENED_FULL = "r2FEXpX6ize"
private const val TEI_UID_VALUE_OPEN_REFERRAL = "Fs6QyeOdDA3"
private const val TEI_UID_VALUE_OPEN_TO_COMPLETE = "qx4yw1EuxmW"
private const val TEI_UID_VALUE_WITH_NOTE = "UtDZmrX5lSd"
private const val TEI_UID_VALUE_TO_DELETE = "SHnmavBQu72"
private const val TEI_UID_VALUE_TO_SCHEDULE = "uh47DXf1St9"

fun prepareTeiCompletedProgrammeAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    startTeiDashboardActivity(CHILD_PROGRAM_UID_VALUE, TEI_UID_VALUE_COMPLETED, rule)
}

fun prepareTeiOpenedForReferralProgrammeAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    startTeiDashboardActivity(TB_PROGRAM_UID, TEI_UID_VALUE_OPEN_REFERRAL, rule)
}

fun prepareTeiOpenedProgrammeAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    startTeiDashboardActivity(CHILD_PROGRAM_UID_VALUE, TEI_UID_VALUE_OPENED, rule)
}

fun prepareTeiOpenedForCompleteProgrammeAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    startTeiDashboardActivity(CHILD_PROGRAM_UID_VALUE, TEI_UID_VALUE_OPEN_TO_COMPLETE, rule)
}

fun prepareTeiWithExistingNoteAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    startTeiDashboardActivity(CHILD_PROGRAM_UID_VALUE, TEI_UID_VALUE_WITH_NOTE, rule)
}

fun prepareTeiOpenedWithFullEventsAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>){
    startTeiDashboardActivity(CHILD_PROGRAM_UID_VALUE, TEI_UID_VALUE_OPENED_FULL, rule)
}

fun prepareTeiToDeleteAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>){
    startTeiDashboardActivity(CHILD_PROGRAM_UID_VALUE, TEI_UID_VALUE_TO_DELETE, rule)
}

fun prepareTeiOpenedWithNoPreviousEventProgrammeAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    startTeiDashboardActivity(TB_PROGRAM_UID, TEI_UID_VALUE_TO_SCHEDULE, rule)
}

fun startTeiDashboardActivity(programUID: String, teiUID: String, rule: ActivityTestRule<TeiDashboardMobileActivity>){
    Intent().apply {
        putExtra(PROGRAM_UID, programUID)
        putExtra(TEI_UID, teiUID)
    }.also { rule.launchActivity(it) }
}

fun prepareChildProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(SearchTETest.CHILD_PROGRAM_UID, SearchTETest.CHILD_PROGRAM_UID_VALUE)
        putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}


