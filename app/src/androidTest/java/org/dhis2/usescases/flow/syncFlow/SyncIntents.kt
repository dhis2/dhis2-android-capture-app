package org.dhis2.usescases.flow.syncFlow

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest

fun prepareTBProgrammeIntentAndLaunchActivity(ruleSearch: ActivityTestRule<SearchTEActivity>) {
    Intent().apply {
        putExtra(PROGRAM_UID, TB_PROGRAM_UID_VALUE)
        putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launchActivity(it) }
}

fun prepareMalariaEventIntentAndLaunchActivity(ruleSearch: ActivityTestRule<ProgramEventDetailActivity>) {
    Intent().apply { putExtra(PROGRAM_UID, ANTENATAL_PROGRAM_UID_VALUE) }.also { ruleSearch.launchActivity(it) }
}

fun prepareFacilityDataSetIntentAndLaunchActivity(ruleSearch: ActivityTestRule<DataSetDetailActivity>) {
    Intent().apply { putExtra(DATASET_UID, MNCH_DATASET_UID_VALUE) }.also { ruleSearch.launchActivity(it) }
}

const val PROGRAM_UID = "PROGRAM_UID"
const val MALARIA_PROGRAM_UID_VALUE = "VBqh0ynB2wv"
const val ANTENATAL_PROGRAM_UID_VALUE = "lxAQ7Zs9VYR"
const val DATASET_UID = "DATASET_UID"
const val FACILITY_DATASET_UID_VALUE = "V8MHeZHIrcP"
const val MNCH_DATASET_UID_VALUE = "EKWVBc5C0ms"
const val TB_PROGRAM_UID_VALUE = "ur1Edk5Oe2n"