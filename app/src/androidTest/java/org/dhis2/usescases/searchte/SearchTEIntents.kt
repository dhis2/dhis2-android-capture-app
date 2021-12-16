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
private const val TB_TE_TYPE_VALUE = "nEenWmSyUEp"
private const val CHILD_TE_TYPE = "TRACKED_ENTITY_UID"

fun prepareChildProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    startSearchActivity(CHILD_PROGRAM_UID_VALUE, CHILD_TE_TYPE_VALUE, ruleSearch)
}

fun prepareTestProgramRulesProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    startSearchActivity(XX_TEST_PROGRAM_RULES_UID_VALUE, PROGRAM_RULES_TE_TYPE_VALUE, ruleSearch)
}

fun prepareTestAdultWomanProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    startSearchActivity(ADULT_WOMAN_PROGRAM_UID_VALUE, ADULT_WOMAN_TE_TYPE_VALUE, ruleSearch)
}

fun prepareTBIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    startSearchActivity(TB_PROGRAM_UID_VALUE, TB_TE_TYPE_VALUE, ruleSearch)
}

fun startSearchActivity(programUID: String?, teType: String, ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, programUID)
        putExtra(CHILD_TE_TYPE, teType)
    }.also { ruleSearch.launchActivity(it) }
}