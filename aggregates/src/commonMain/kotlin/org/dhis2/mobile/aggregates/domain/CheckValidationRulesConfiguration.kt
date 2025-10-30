package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.MANDATORY
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.NONE
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.OPTIONAL

internal class CheckValidationRulesConfiguration(
    private val dataSetUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(): ValidationRulesConfiguration {
        val hasValidationRules =
            dataSetInstanceRepository.checkIfHasValidationRules(
                dataSetUid = dataSetUid,
            )

        return if (hasValidationRules) {
            val mandatory =
                dataSetInstanceRepository.areValidationRulesMandatory(
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
