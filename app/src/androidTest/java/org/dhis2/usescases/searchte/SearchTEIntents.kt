package org.dhis2.usescases.searchte

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
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

fun prepareChildProgrammeIntentAndLaunchActivity(ruleSearch: LazyActivityScenarioRule<SearchTEActivity>) {
    startSearchActivity(CHILD_PROGRAM_UID_VALUE, CHILD_TE_TYPE_VALUE, ruleSearch)
}

fun prepareTestProgramRulesProgrammeIntentAndLaunchActivity(ruleSearch: LazyActivityScenarioRule<SearchTEActivity>) {
    startSearchActivity(XX_TEST_PROGRAM_RULES_UID_VALUE, PROGRAM_RULES_TE_TYPE_VALUE, ruleSearch)
}

fun prepareTestAdultWomanProgrammeIntentAndLaunchActivity(ruleSearch: LazyActivityScenarioRule<SearchTEActivity>) {
    startSearchActivity(ADULT_WOMAN_PROGRAM_UID_VALUE, ADULT_WOMAN_TE_TYPE_VALUE, ruleSearch)
}

fun prepareTBIntentAndLaunchActivity(ruleSearch: LazyActivityScenarioRule<SearchTEActivity>) {
    startSearchActivity(TB_PROGRAM_UID_VALUE, TB_TE_TYPE_VALUE, ruleSearch)
}

fun startSearchActivity(
    programUID: String?,
    teType: String,
    ruleSearch: LazyActivityScenarioRule<SearchTEActivity>
) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        SearchTEActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, programUID)
        putExtra(CHILD_TE_TYPE, teType)
    }.also { ruleSearch.launch(it) }
}