package org.dhis2.usescases.flow.syncFlow

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.SearchTETest

fun prepareTBProgrammeIntentAndLaunchActivity(ruleSearch: LazyActivityScenarioRule<SearchTEActivity>) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        SearchTEActivity::class.java,
    ).apply {
        putExtra(PROGRAM_UID, TB_PROGRAM_UID_VALUE)
        putExtra(SearchTETest.CHILD_TE_TYPE, SearchTETest.CHILD_TE_TYPE_VALUE)
    }.also { ruleSearch.launch(it) }
}

fun prepareMalariaEventIntentAndLaunchActivity(ruleSearch: LazyActivityScenarioRule<ProgramEventDetailActivity>) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        ProgramEventDetailActivity::class.java,
    ).apply { putExtra(PROGRAM_UID, ANTENATAL_PROGRAM_UID_VALUE) }.also { ruleSearch.launch(it) }
}

fun prepareFacilityDataSetIntentAndLaunchActivity(ruleSearch: LazyActivityScenarioRule<DataSetDetailActivity>) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        DataSetDetailActivity::class.java,
    ).apply { putExtra(DATASET_UID, MNCH_DATASET_UID_VALUE) }
        .also { ruleSearch.launch(it) }
}

const val PROGRAM_UID = "PROGRAM_UID"
const val ANTENATAL_PROGRAM_UID_VALUE = "lxAQ7Zs9VYR"
const val DATASET_UID = "DATASET_UID"
const val MNCH_DATASET_UID_VALUE = "EKWVBc5C0ms"
const val TB_PROGRAM_UID_VALUE = "ur1Edk5Oe2n"