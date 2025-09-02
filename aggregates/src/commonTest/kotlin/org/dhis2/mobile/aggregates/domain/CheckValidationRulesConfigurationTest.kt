package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration
import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import kotlin.test.Test

class CheckValidationRulesConfigurationTest {
    private val dataSetInstanceRepository: DataSetInstanceRepository = mock()

    private val dataSetUid = "dataSetUid"
    internal lateinit var checkValidationRulesConfiguration: CheckValidationRulesConfiguration

    @Before
    fun setUp() {
        checkValidationRulesConfiguration =
            CheckValidationRulesConfiguration(dataSetUid, dataSetInstanceRepository)
    }

    @Test
    fun `should return NONE when there are no validation rules`() =
        runTest {
            // Given dataset has no validation rules
            whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn false

            // When checking validation rules
            val result = checkValidationRulesConfiguration()

            // Then return none
            assertEquals(ValidationRulesConfiguration.NONE, result)
        }

    @Test
    fun `should return Mandatory when validation rules are mandatory`() =
        runTest {
            // Given dataset has mandatory validation rules
            whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn true
            whenever(dataSetInstanceRepository.areValidationRulesMandatory(dataSetUid)) doReturn true

            // When checking validation rules
            val result = checkValidationRulesConfiguration()

            // Then return MANDATORY
            assertEquals(ValidationRulesConfiguration.MANDATORY, result)
        }

    @Test
    fun `should return Optional when validation rules are optional`() =
        runTest {
            // Given dataset has optional validation rules
            whenever(dataSetInstanceRepository.checkIfHasValidationRules(dataSetUid)) doReturn true
            whenever(dataSetInstanceRepository.areValidationRulesMandatory(dataSetUid)) doReturn false

            // When checking validation rules
            val result = checkValidationRulesConfiguration()

            // Then return OPTIONAL
            assertEquals(ValidationRulesConfiguration.OPTIONAL, result)
        }
}
