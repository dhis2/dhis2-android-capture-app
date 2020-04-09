package org.dhis2.usescases.teidashboard

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

private const val PROGRAM_UID = "PROGRAM_UID"
private const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"
private const val TB_PROGRAM_UI = "ur1Edk5Oe2n"
private const val TEI_UID = "TEI_UID"
private const val TEI_UID_VALUE_COMPLETED = "vOxUH373fy5"
private const val TEI_UID_VALUE_OPENED = "Pqv3LrNECkn"
private const val TEI_UID_VALUE_OPENED_FULL = "r2FEXpX6ize"
private const val TEI_UID_VALUE_OPEN_REFERRAL = "Fs6QyeOdDA3" //"PQfMcpmXeFE"
private const val TEI_UID_VALUE_OPEN_TO_COMPLETE = "qx4yw1EuxmW"
private const val TEI_UID_VALUE_WITH_NOTE = "UtDZmrX5lSd"
private const val TEI_UID_VALUE_TO_DELETE = "SHnmavBQu72"
private const val TEI_UID_VALUE_TO_SCHEDULE = "uh47DXf1St9"

fun prepareTeiCompletedProgrammeIntentAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
        putExtra(TEI_UID, TEI_UID_VALUE_COMPLETED)
    }.also { rule.launchActivity(it) }
}

fun prepareTeiOpenedForReferralProgrammeIntentAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, TB_PROGRAM_UI)
        putExtra(TEI_UID, TEI_UID_VALUE_OPEN_REFERRAL)
    }.also { rule.launchActivity(it) }
}

fun prepareTeiOpenedProgrammeIntentAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
        putExtra(TEI_UID, TEI_UID_VALUE_OPENED)
    }.also { rule.launchActivity(it) }
}

fun prepareTeiOpenedForCompleteProgrammeIntentAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
        putExtra(TEI_UID, TEI_UID_VALUE_OPEN_TO_COMPLETE)
    }.also { rule.launchActivity(it) }
}

fun prepareTeiWithExistingNoteIntentAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
        putExtra(TEI_UID, TEI_UID_VALUE_WITH_NOTE)
    }.also { rule.launchActivity(it) }
}

fun prepareTeiOpenedWithFullEvents(rule: ActivityTestRule<TeiDashboardMobileActivity>){
    Intent().apply{
        putExtra(PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
        putExtra(TEI_UID, TEI_UID_VALUE_OPENED_FULL)
    }.also { rule.launchActivity(it) }
}

fun prepareChildProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(SearchTETest.CHILD_PROGRAM_UID, SearchTETest.CHILD_PROGRAM_UID_VALUE)
        putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}

fun prepareTeiToDelete(rule: ActivityTestRule<TeiDashboardMobileActivity>){
    Intent().apply{
        putExtra(PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
        putExtra(TEI_UID, TEI_UID_VALUE_TO_DELETE)
    }.also { rule.launchActivity(it) }
}

fun prepareTeiOpenedWithNoPreviousEventProgrammeIntentAndLaunchActivity(rule: ActivityTestRule<TeiDashboardMobileActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, TB_PROGRAM_UI)
        putExtra(TEI_UID, TEI_UID_VALUE_TO_SCHEDULE)
    }.also { rule.launchActivity(it) }
}


