package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.ERROR
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.MISSING_MANDATORY_FIELDS
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.MISSING_MANDATORY_FIELDS_COMBINATION
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.SUCCESS
import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import kotlin.test.Test

class CompleteDataSetTest {
    private val dataSetInstanceRepository: DataSetInstanceRepository = mock()

    private val dataSetUid = "dataSetUid"
    private val periodId = "periodId"
    private val orgUnitUid = "orgUnitUid"
    private val attrOptionComboUid = "attrOptionComboUid"
    internal lateinit var completeDataSet: CompleteDataSet

    @Before
    fun setUp() {
        completeDataSet =
            CompleteDataSet(
                dataSetUid,
                periodId,
                orgUnitUid,
                attrOptionComboUid,
                dataSetInstanceRepository,
            )
    }

    @Test
    fun `should return success when everything is right`() =
        runTest {
            // Given dataset instance has no missing fields
            whenever(
                dataSetInstanceRepository.checkIfHasMissingMandatoryFields(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn false

            whenever(
                dataSetInstanceRepository.checkIfHasMissingMandatoryFieldsCombination(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn false

            // And can be completed
            whenever(
                dataSetInstanceRepository.completeDataset(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn Result.success(Unit)

            // When user tries to complete the dataset
            val result = completeDataSet()

            // Then completion is successful
            assertEquals(SUCCESS, result)
        }

    @Test
    fun `should return error when there is any unhandled error`() =
        runTest {
            // Given dataset instance has no missing fields
            whenever(
                dataSetInstanceRepository.checkIfHasMissingMandatoryFields(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn false

            whenever(
                dataSetInstanceRepository.checkIfHasMissingMandatoryFieldsCombination(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn false

            // And has an unknown error
            whenever(
                dataSetInstanceRepository.completeDataset(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn Result.failure(Throwable())

            // When user tries to complete the dataset
            val result = completeDataSet()

            // Then completion fails
            assertEquals(ERROR, result)
        }

    @Test
    fun `should return missing mandatory fields information`() =
        runTest {
            // Given dataset instance has missing mandatory fields
            whenever(
                dataSetInstanceRepository.checkIfHasMissingMandatoryFields(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn true

            // When user tries to complete the dataset
            val result = completeDataSet()

            // Then completion fails by missing mandatory fields
            assertEquals(MISSING_MANDATORY_FIELDS, result)
        }

    @Test
    fun `should return missing mandatory fields combination information`() =
        runTest {
            // Given dataset instance has missing fields combination
            whenever(
                dataSetInstanceRepository.checkIfHasMissingMandatoryFields(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn false

            whenever(
                dataSetInstanceRepository.checkIfHasMissingMandatoryFieldsCombination(
                    dataSetUid,
                    periodId,
                    orgUnitUid,
                    attrOptionComboUid,
                ),
            ) doReturn true

            // When user tries to complete the dataset
            val result = completeDataSet()

            // Then completion fails by missing fields combination
            assertEquals(MISSING_MANDATORY_FIELDS_COMBINATION, result)
        }
}
