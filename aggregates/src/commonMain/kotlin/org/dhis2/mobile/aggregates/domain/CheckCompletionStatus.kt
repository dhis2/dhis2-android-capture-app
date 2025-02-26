package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.COMPLETED
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.NOT_COMPLETED

internal class CheckCompletionStatus(
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {

    suspend operator fun invoke(): DataSetCompletionStatus {
        val isComplete = dataSetInstanceRepository.isComplete(
            dataSetUid = dataSetUid,
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            attrOptionComboUid = attrOptionComboUid,
        )

        return if (isComplete) {
            COMPLETED
        } else {
            NOT_COMPLETED
        }
    }
}
