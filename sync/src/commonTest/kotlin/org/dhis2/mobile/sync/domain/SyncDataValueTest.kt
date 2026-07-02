package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.sync.data.SyncDataValueRepository
import org.dhis2.mobile.sync.model.SyncDataValueInput
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

class SyncDataValueTest {
    private val repository: SyncDataValueRepository = mock()
    private val useCase = SyncDataValue(repository)

    private val input =
        SyncDataValueInput(
            dataSetUid = "dataSetUid",
            orgUnitUid = "orgUnitUid",
            periodId = "periodId",
            attrOptionComboUid = "attrOptionComboUid",
            categoryOptionComboUid = listOf("combo1", "combo2"),
        )

    @Test
    fun `should return success and call all repository methods in order`() =
        runBlocking {
            val result = useCase(input)

            assertTrue(result.isSuccess)
            verify(repository).uploadDataValues(
                dataSetUid = input.dataSetUid,
                orgUnitUid = input.orgUnitUid,
                periodId = input.periodId,
                attributeOptionComboUid = input.attrOptionComboUid,
                categoryOptionComboUids = input.categoryOptionComboUid,
            )
            verify(repository).uploadCompleteRegistrations(
                dataSetUid = input.dataSetUid,
                orgUnitUid = input.orgUnitUid,
                periodId = input.periodId,
                attributeOptionComboUid = input.attrOptionComboUid,
            )
            verify(repository).downloadDataValues(
                dataSetUid = input.dataSetUid,
                orgUnitUid = input.orgUnitUid,
                periodId = input.periodId,
                attributeOptionComboUid = input.attrOptionComboUid,
            )
        }

    @Test
    fun `should return failure when uploadDataValues throws`() =
        runBlocking {
            whenever(
                repository.uploadDataValues(
                    dataSetUid = input.dataSetUid,
                    orgUnitUid = input.orgUnitUid,
                    periodId = input.periodId,
                    attributeOptionComboUid = input.attrOptionComboUid,
                    categoryOptionComboUids = input.categoryOptionComboUid,
                ),
            ).thenThrow(RuntimeException("upload error"))

            val result = useCase(input)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when uploadCompleteRegistrations throws`() =
        runBlocking {
            whenever(
                repository.uploadCompleteRegistrations(
                    dataSetUid = input.dataSetUid,
                    orgUnitUid = input.orgUnitUid,
                    periodId = input.periodId,
                    attributeOptionComboUid = input.attrOptionComboUid,
                ),
            ).thenThrow(RuntimeException("complete registration error"))

            val result = useCase(input)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when downloadDataValues throws`() =
        runBlocking {
            whenever(
                repository.downloadDataValues(
                    dataSetUid = input.dataSetUid,
                    orgUnitUid = input.orgUnitUid,
                    periodId = input.periodId,
                    attributeOptionComboUid = input.attrOptionComboUid,
                ),
            ).thenThrow(RuntimeException("download error"))

            val result = useCase(input)

            assertTrue(result.isFailure)
        }
}
