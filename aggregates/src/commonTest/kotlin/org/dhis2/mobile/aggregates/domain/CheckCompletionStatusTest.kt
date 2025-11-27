package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.COMPLETED
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.NOT_COMPLETED_EDITABLE
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.NOT_COMPLETED_NOT_EDITABLE
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class CheckCompletionStatusTest {
    private val dataSetInstanceRepository: DataSetInstanceRepository = mock()

    private val dataSetUid = "dataSetUid"
    private val periodId = "periodId"
    private val orgUnitUid = "orgUnitUid"
    private val attrOptionComboUid = "attrOptionComboUid"
    internal lateinit var checkCompletionStatus: CheckCompletionStatus

    @Before
    fun setUp() {
        checkCompletionStatus =
            CheckCompletionStatus(
                dataSetUid,
                periodId,
                orgUnitUid,
                attrOptionComboUid,
                dataSetInstanceRepository,
            )
    }

    @Test
    fun `should return dataset instance is completed`() =
        runTest {
            // Given dataset instance is completed
            whenever(
                dataSetInstanceRepository.isComplete(
                    dataSetUid = dataSetUid,
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                ),
            ) doReturn true

            whenever(
                dataSetInstanceRepository.isEditable(
                    dataSetUid = dataSetUid,
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                ),
            ) doReturn true

            // When user check if dataset is completed
            val result = checkCompletionStatus()

            // Then return completed
            assertEquals(COMPLETED, result)
        }

    @Test
    fun `should return dataset instance is not completed and editable`() =
        runTest {
            // Given dataset instance is not completed
            whenever(
                dataSetInstanceRepository.isComplete(
                    dataSetUid = dataSetUid,
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                ),
            ) doReturn false

            whenever(
                dataSetInstanceRepository.isEditable(
                    dataSetUid = dataSetUid,
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                ),
            ) doReturn true

            // When user check if dataset is completed
            val result = checkCompletionStatus()

            // Then return completed
            assertEquals(NOT_COMPLETED_EDITABLE, result)
        }

    @Test
    fun `should return dataset instance is not completed ando not editable`() =
        runTest {
            // Given dataset instance is not completed
            whenever(
                dataSetInstanceRepository.isComplete(
                    dataSetUid = dataSetUid,
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                ),
            ) doReturn false

            whenever(
                dataSetInstanceRepository.isEditable(
                    dataSetUid = dataSetUid,
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                ),
            ) doReturn false

            // When user check if dataset is completed
            val result = checkCompletionStatus()

            // Then return completed
            assertEquals(NOT_COMPLETED_NOT_EDITABLE, result)
        }
}
