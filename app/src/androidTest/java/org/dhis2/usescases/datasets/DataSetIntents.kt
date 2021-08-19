package org.dhis2.usescases.datasets

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.utils.Constants.ACCESS_DATA
import org.dhis2.utils.Constants.DATASET_UID
import org.dhis2.utils.Constants.DATA_SET_NAME

fun startDataSetActivity(
    dataSetUid: String,
    orgUnitUid: String,
    orgUnitName: String,
    periodTypeName: String,
    periodInitialDate: String,
    periodId: String,
    catOptCombo: String,
    rule: ActivityTestRule<DataSetTableActivity>
) {
    Intent().apply {
        putExtras(
            DataSetTableActivity.getBundle(
                dataSetUid,
                orgUnitUid,
                orgUnitName,
                periodTypeName,
                periodInitialDate,
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
    rule: ActivityTestRule<DataSetDetailActivity>
) {
    Intent().apply {
        putExtra(DATASET_UID, dataSetUid)
        putExtra(DATA_SET_NAME, dataSetName)
        putExtra(ACCESS_DATA, true)
    }.also { rule.launchActivity(it) }
}
