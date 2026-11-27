package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration

internal class CheckValidationRulesConfiguration(
    private val dataSetUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(): ValidationRulesConfiguration = dataSetInstanceRepository.getValidationRulesConfiguration(dataSetUid)
}
