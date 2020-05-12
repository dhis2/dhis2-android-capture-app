package org.dhis2.usescases.datasets.dataSetTable.dataSetDetail

import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.period.Period

interface DataSetDetailView {
    fun openSyncDialog()
    fun setCatOptComboName(catComboName: String)
    fun setDataSetDetails(
        dataSetInstance: DataSetInstance,
        period: Period
    )
    fun hideCatOptCombo()
    fun setStyle(style: ObjectStyle?)
}