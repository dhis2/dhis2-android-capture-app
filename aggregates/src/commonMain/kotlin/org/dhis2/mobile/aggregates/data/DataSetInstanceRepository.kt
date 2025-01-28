package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.DataSetDetails

interface DataSetInstanceRepository {
    fun getDataSetInstance(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): DataSetDetails
}
