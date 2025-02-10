package org.dhis2.usescases.datasets

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.commons.Constants.ACCESS_DATA
import org.dhis2.commons.Constants.DATASET_UID
import org.dhis2.commons.Constants.DATA_SET_NAME
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity

fun startDataSetActivity(
    dataSetUid: String,
    orgUnitUid: String,
    periodId: String,
    catOptCombo: String,
    rule: ActivityTestRule<DataSetTableActivity>
) {
    Intent().apply {
        putExtras(
            DataSetTableActivity.getBundle(
                dataSetUid,
                orgUnitUid,
                periodId,
                catOptCombo
            )
        )
    }.also {
        rule.launchActivity(it)
    }
}

fun startDataSetDetailActivity(
    dataSetUid: String,
    dataSetName: String,
    rule: LazyActivityScenarioRule<DataSetDetailActivity>
) {
    Intent(
        ApplicationProvider.getApplicationContext(),
        DataSetDetailActivity::class.java
    ).apply {
        putExtra(DATASET_UID, dataSetUid)
        putExtra(DATA_SET_NAME, dataSetName)
        putExtra(ACCESS_DATA, true)
    }.also { rule.launch(it) }
}
