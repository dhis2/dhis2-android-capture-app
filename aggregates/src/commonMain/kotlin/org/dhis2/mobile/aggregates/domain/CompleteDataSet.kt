package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.ERROR
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.MISSING_MANDATORY_FIELDS
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.MISSING_MANDATORY_FIELDS_COMBINATION
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.SUCCESS

internal class CompleteDataSet(
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(): DataSetMandatoryFieldsStatus =
        if (checkIfHasMissingMandatoryFields()) {
            MISSING_MANDATORY_FIELDS
        } else if (checkIfHasMissingMandatoryFieldsCombination()) {
            MISSING_MANDATORY_FIELDS_COMBINATION
        } else {
            dataSetInstanceRepository
                .completeDataset(
                    dataSetUid = dataSetUid,
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attributeOptionComboUid = attrOptionComboUid,
                ).fold(
                    onSuccess = {
                        SUCCESS
                    },
                    onFailure = {
                        ERROR
                    },
                )
        }

    private suspend fun checkIfHasMissingMandatoryFields(): Boolean {
        val hasMissingMandatoryFields =
            dataSetInstanceRepository.checkIfHasMissingMandatoryFields(
                dataSetUid = dataSetUid,
                periodId = periodId,
                orgUnitUid = orgUnitUid,
                attributeOptionComboUid = attrOptionComboUid,
            )
        return hasMissingMandatoryFields
    }

    private suspend fun checkIfHasMissingMandatoryFieldsCombination(): Boolean {
        val hasMissingMandatoryFieldsCombination =
            dataSetInstanceRepository.checkIfHasMissingMandatoryFieldsCombination(
                dataSetUid = dataSetUid,
                periodId = periodId,
                orgUnitUid = orgUnitUid,
                attributeOptionComboUid = attrOptionComboUid,
            )
        return hasMissingMandatoryFieldsCombination
    }
}
