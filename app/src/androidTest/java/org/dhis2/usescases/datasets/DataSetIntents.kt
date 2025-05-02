package org.dhis2.usescases.datasets

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.dhis2.LazyActivityScenarioRule
import org.dhis2.commons.Constants.ACCESS_DATA
import org.dhis2.commons.Constants.DATASET_UID
import org.dhis2.commons.Constants.DATA_SET_NAME
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity

internal fun startDataSetDetailActivity(
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
