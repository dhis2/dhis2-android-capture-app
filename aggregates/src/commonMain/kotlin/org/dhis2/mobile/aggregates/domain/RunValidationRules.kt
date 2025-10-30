package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.ValidationResultStatus
import org.dhis2.mobile.aggregates.model.ValidationRulesResult

internal class RunValidationRules(
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(): ValidationRulesResult {
        val validationResult =
            dataSetInstanceRepository.runValidationRules(
                dataSetUid,
                periodId,
                orgUnitUid,
                attrOptionComboUid,
            )
        return when (validationResult.validationResultStatus) {
            ValidationResultStatus.OK -> {
                validationResult
            }

            ValidationResultStatus.ERROR -> {
                if (dataSetInstanceRepository.areValidationRulesMandatory(dataSetUid)) {
                    validationResult.copy(mandatory = true)
                } else {
                    validationResult
                }
            }
        }
    }
}
