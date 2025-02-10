package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.ValueParser

internal class GetDataValue(
    private val orgUnitUid: String,
    private val periodId: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
    private val valueParser: ValueParser,
) {
    suspend operator fun invoke(
        dataElementUid: String,
        categoryOptionComboUid: String,
    ) = dataSetInstanceRepository.cellValue(
        periodId = periodId,
        orgUnitUid = orgUnitUid,
        dataElementUid = dataElementUid,
        categoryOptionComboUid = categoryOptionComboUid,
        attrOptionComboUid = attrOptionComboUid,
    )?.let { value ->
        valueParser.parseValue(dataElementUid, value)
    }
}
