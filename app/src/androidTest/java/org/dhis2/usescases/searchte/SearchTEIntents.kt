package org.dhis2.usescases.searchte

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity

private const val PROGRAM_UID = "PROGRAM_UID"
private const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"
private const val XX_TEST_PROGRAM_RULES_UID_VALUE = "jIT6KcSZiAN"
private const val ADULT_WOMAN_PROGRAM_UID_VALUE = "uy2gU8kT1jF"
private const val TB_PROGRAM_UID_VALUE = "ur1Edk5Oe2n"

private const val CHILD_TE_TYPE_VALUE = "nEenWmSyUEp"
private const val PROGRAM_RULES_TE_TYPE_VALUE = "nEenWmSyUEp"
private const val ADULT_WOMAN_TE_TYPE_VALUE = "nEenWmSyUEp"
private const val CHILD_TE_TYPE = "TRACKED_ENTITY_UID"

fun prepareChildProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
        putExtra(CHILD_TE_TYPE, CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}

fun prepareTestProgramRulesProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, XX_TEST_PROGRAM_RULES_UID_VALUE)
        putExtra(CHILD_TE_TYPE, PROGRAM_RULES_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}

fun prepareTestAdultWomanProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, ADULT_WOMAN_PROGRAM_UID_VALUE)
        putExtra(CHILD_TE_TYPE, ADULT_WOMAN_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}

fun prepareTBIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, TB_PROGRAM_UID_VALUE)
        putExtra(CHILD_TE_TYPE, CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}

/*
fun prepareChildProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(SearchTETest.PROGRAM_UID, SearchTETest.CHILD_PROGRAM_UID_VALUE)
        putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}*/
