package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus

internal class CheckMandatoryFieldsStatus(
    private val dataSetUid: String,
) {
    suspend operator fun invoke(): DataSetMandatoryFieldsStatus {
        return DataSetMandatoryFieldsStatus.MISSING_MANDATORY_FIELDS
    }
}
