package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetValidationRulesConfiguration
import org.junit.Assert.assertEquals
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import kotlin.test.Test

class CheckValidationRulesTest {

    private val dataSetInstanceRepository: DataSetInstanceRepository = mock()

    @Test
    fun `should return NONE when there are no validation rules`() = runTest {
        // Given
        val dataSetUid = "dataSetUid"
        val checkValidationRules = CheckValidationRules(dataSetUid, dataSetInstanceRepository)
        whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn false

        // When
        val result = checkValidationRules()

        // Then
        assertEquals(DataSetValidationRulesConfiguration.NONE, result)
    }

    @Test
    fun `should return Mandatory when validation rules are mandatory`() = runTest {
        // Given
        val dataSetUid = "dataSetUid"
        val checkValidationRules = CheckValidationRules(dataSetUid, dataSetInstanceRepository)
        whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn true
        whenever(dataSetInstanceRepository.areValidationRulesMandatory(dataSetUid)) doReturn true

        // When
        val result = checkValidationRules()

        // Then
        assertEquals(DataSetValidationRulesConfiguration.MANDATORY, result)
    }

    @Test
    fun `should return Optional when validation rules are optional`() = runTest {
        // Given
        val dataSetUid = "dataSetUid"
        val checkValidationRules = CheckValidationRules(dataSetUid, dataSetInstanceRepository)
        whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn true
        whenever(dataSetInstanceRepository.areValidationRulesMandatory(dataSetUid)) doReturn false

        // When
        val result = checkValidationRules()

        // Then
        assertEquals(DataSetValidationRulesConfiguration.OPTIONAL, result)
    }
}
