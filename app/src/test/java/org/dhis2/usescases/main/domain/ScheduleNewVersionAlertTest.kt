package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ScheduleNewVersionAlertTest {
    private val workManagerController: WorkManagerController = mock()
    private val versionRepository: VersionRepository = mock()
    private lateinit var scheduleNewVersionAlert: ScheduleNewVersionAlert

    @Before
    fun setUp() {
        scheduleNewVersionAlert =
            ScheduleNewVersionAlert(
                workManagerController = workManagerController,
                versionRepository = versionRepository,
            )
    }

    @Test
    fun `should schedule new version alert and remove version info`() =
        runTest {
            val result = scheduleNewVersionAlert()

            assertTrue(result.isSuccess)

            verify(workManagerController).beginUniqueWork(any())
            verify(versionRepository).removeVersionInfo()
        }

    @Test
    fun `should return failure when remove version info fails`() =
        runTest {
            val exception = DomainError.DataBaseError("Error")
            given(versionRepository.removeVersionInfo()).willAnswer {
                throw exception
            }

            val result = scheduleNewVersionAlert()

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
}
