package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetValidationRulesConfiguration
import org.dhis2.mobile.aggregates.model.DataSetValidationRulesConfiguration.MANDATORY
import org.dhis2.mobile.aggregates.model.DataSetValidationRulesConfiguration.NONE
import org.dhis2.mobile.aggregates.model.DataSetValidationRulesConfiguration.OPTIONAL

internal class CheckValidationRules(
    private val dataSetUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(): DataSetValidationRulesConfiguration {
        val hasValidationRules = dataSetInstanceRepository.checkIfHasValidationRules(
            dataSetUid = dataSetUid,
        )

        return if (hasValidationRules) {
            val mandatory = dataSetInstanceRepository.areValidationRulesMandatory(
                dataSetUid = dataSetUid,
            )
            if (mandatory) {
                MANDATORY
            } else {
                OPTIONAL
            }
        } else {
            NONE
        }
    }
}
