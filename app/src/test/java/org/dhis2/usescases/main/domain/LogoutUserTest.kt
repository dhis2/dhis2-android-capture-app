package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.HomeRepository
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LogoutUserTest {
    private val repository: HomeRepository = mock()
    private val workManagerController: WorkManagerController = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val filterManager: FilterManager = mock()

    private lateinit var logoutUser: LogoutUser

    @Before
    fun setUp() {
        logoutUser =
            LogoutUser(
                repository,
                workManagerController,
                syncStatusController,
                filterManager,
            )
    }

    @Test
    fun `GIVEN the user logs out WHEN success THEN account count is returned`() =
        runTest {
            whenever(repository.logOut()) doReturn Result.success(Unit)
            whenever(repository.accountsCount()) doReturn 1
            val result = logoutUser()
            verify(workManagerController).cancelAllWorkAndWait()
            verify(syncStatusController).restore()
            verify(filterManager).clearAllFilters()
            verify(repository).clearSessionLock()
            verify(repository).logOut()
            verify(repository).accountsCount()

            assertTrue(result.isSuccess && result.getOrNull() == 1)
        }

    @Test
    fun `GIVEN the user logs out WHEN domain error THEN exception is returned`() =
        runTest {
            val testException = DomainError.UnexpectedError("test")
            whenever(repository.clearSessionLock()) doReturn Result.failure(testException)
            val result = logoutUser()
            verify(workManagerController).cancelAllWorkAndWait()
            verify(syncStatusController).restore()
            verify(filterManager).clearAllFilters()
            verify(repository).clearSessionLock()
            verify(repository, never()).logOut()
            verify(repository, never()).accountsCount()

            assertTrue(result.isFailure && result.exceptionOrNull() == testException)
        }

    @Test
    fun `GIVEN the user logs out WHEN non-domain error THEN exception is propagated`() =
        runTest {
            val testException = RuntimeException("unexpected error")
            whenever(repository.logOut()).thenThrow(testException)

            try {
                logoutUser()
                fail("Exception should have been thrown")
            } catch (e: RuntimeException) {
                assertTrue(e == testException)
            }

            verify(workManagerController).cancelAllWorkAndWait()
            verify(syncStatusController).restore()
            verify(filterManager).clearAllFilters()
            verify(repository).clearSessionLock()
            verify(repository).logOut()
            verify(repository, never()).accountsCount()
        }
}
