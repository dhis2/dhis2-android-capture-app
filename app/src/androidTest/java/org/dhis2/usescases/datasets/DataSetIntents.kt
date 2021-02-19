package org.dhis2.usescases.datasets

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity

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
