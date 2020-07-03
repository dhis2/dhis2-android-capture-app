package org.dhis2.usescases.syncFlow

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest
import org.dhis2.usescases.settingsprogram.SettingsProgramActivity


// "EQAYnCupJ1J" // "EaOyKGOIGRp" //"kfwLSxq7mXk" // "tIJu6iqQxNV" //"osF4RF4EiqP" //Heather Greene

fun prepareChildProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(SearchTETest.CHILD_PROGRAM_UID, SearchTETest.CHILD_PROGRAM_UID_VALUE)
        putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}

fun prepareTBProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, SearchTETest.TB_PROGRAM_UID_VALUE)
        putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}

fun prepareMalariaEventIntentAndLaunchActivity(ruleSearch: ActivityTestRule<ProgramEventDetailActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, ANTENATAL_PROGRAM_UID_VALUE)
      //  putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}


// ProgramEventDetailActivity
// SettingsProgramActivity a5e67163090
const val PROGRAM_UID = "PROGRAM_UID"
const val EVENT_WITHOUT_REGISTRATION_UID = "PROGRAM_UID"
const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"
const val MALARIA_PROGRAM_UID_VALUE = "VBqh0ynB2wv"
const val ANTENATAL_PROGRAM_UID_VALUE = "lxAQ7Zs9VYR"