package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.main.HomeRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LogoutUserTest {
    private val repository: HomeRepository = mock()
    private val workManagerController: WorkManagerController = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val filterManager: FilterManager = mock()
    private val preferences: PreferenceProvider = mock()

    private lateinit var logoutUser: LogoutUser

    @Before
    fun setUp() {
        logoutUser =
            LogoutUser(
                repository,
                workManagerController,
                syncStatusController,
                filterManager,
                preferences,
            )
    }

    @Test
    fun `GIVEN the user logs out WHEN success THEN account count is returned`() =
        runTest {
            whenever(repository.logOut()) doReturn Result.success(Unit)
            whenever(repository.accountsCount()) doReturn 1
            val result = logoutUser()
            verify(workManagerController).cancelAllWork()
            verify(syncStatusController).restore()
            verify(filterManager).clearAllFilters()
            verify(repository).clearSessionLock()
            verify(repository).logOut()
            verify(repository).accountsCount()

            assertTrue(result.isSuccess && result.getOrNull() == 1)
        }

    @Test
    fun `GIVEN the user logs out WHEN failure THEN exception is returned`() =
        runTest {
            val testException = Exception("test")
            whenever(repository.logOut()) doReturn Result.failure(testException)
            val result = logoutUser()
            verify(workManagerController).cancelAllWork()
            verify(syncStatusController).restore()
            verify(filterManager).clearAllFilters()
            verify(repository).clearSessionLock()
            verify(repository).logOut()
            verify(repository, times(0)).accountsCount()

            assertTrue(result.isFailure && result.exceptionOrNull() == testException)
        }
}
