package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration
import org.junit.Assert.assertEquals
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import kotlin.test.Test

class CheckValidationRulesConfigurationTest {

    private val dataSetInstanceRepository: DataSetInstanceRepository = mock()

    @Test
    fun `should return NONE when there are no validation rules`() = runTest {
        // Given
        val dataSetUid = "dataSetUid"
        val checkValidationRulesConfiguration = CheckValidationRulesConfiguration(dataSetUid, dataSetInstanceRepository)
        whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn false

        // When
        val result = checkValidationRulesConfiguration()

        // Then
        assertEquals(ValidationRulesConfiguration.NONE, result)
    }

    @Test
    fun `should return Mandatory when validation rules are mandatory`() = runTest {
        // Given
        val dataSetUid = "dataSetUid"
        val checkValidationRulesConfiguration = CheckValidationRulesConfiguration(dataSetUid, dataSetInstanceRepository)
        whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn true
        whenever(dataSetInstanceRepository.areValidationRulesMandatory(dataSetUid)) doReturn true

        // When
        val result = checkValidationRulesConfiguration()

        // Then
        assertEquals(ValidationRulesConfiguration.MANDATORY, result)
    }

    @Test
    fun `should return Optional when validation rules are optional`() = runTest {
        // Given
        val dataSetUid = "dataSetUid"
        val checkValidationRulesConfiguration = CheckValidationRulesConfiguration(dataSetUid, dataSetInstanceRepository)
        whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn true
        whenever(dataSetInstanceRepository.areValidationRulesMandatory(dataSetUid)) doReturn false

        // When
        val result = checkValidationRulesConfiguration()

        // Then
        assertEquals(ValidationRulesConfiguration.OPTIONAL, result)
    }
}
