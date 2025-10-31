package org.dhis2.usescases.main.domain

import junit.framework.Assert.assertTrue
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer
import kotlin.io.path.createTempFile

class DeleteAccountTest {
    private val workManagerController: WorkManagerController = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val filterManager: FilterManager = mock()
    private val repository: HomeRepository = mock()
    private lateinit var deleteAccount: DeleteAccount

    @Before
    fun setUp() {
        deleteAccount = DeleteAccount(
            workManagerController = workManagerController,
            syncStatusController = syncStatusController,
            filterManager = filterManager,
            repository = repository
        )
    }

    @Test
    fun `should successfully return and clear all data`() = runTest {
        val cacheFile = createTempFile().toFile()
        whenever(repository.clearCache(any())) doReturn true
        whenever(repository.accountsCount()) doReturn 3
        with(deleteAccount(cacheFile)) {
            verify(workManagerController).cancelAllWork()
            verify(workManagerController).pruneWork()
            verify(syncStatusController).restore()
            verify(filterManager).clearAllFilters()
            verify(repository).clearCache(cacheFile)
            verify(repository).clearPreferences()
            verify(repository).wipeAll()
            verify(repository).deleteCurrentAccount()
            assertTrue(isSuccess)
            assertTrue(getOrNull() == 3)
        }
    }

    @Test
    fun `should return failure when domain exception is thrown`() = runTest {
        val cacheFile = createTempFile().toFile()
        whenever(repository.clearCache(any())) doReturn true
        given(repository.deleteCurrentAccount()) willAnswer { throw DomainError.DataBaseError("Test") }
        with(deleteAccount(cacheFile)) {
            verify(workManagerController).cancelAllWork()
            verify(workManagerController).pruneWork()
            verify(syncStatusController).restore()
            verify(filterManager).clearAllFilters()
            verify(repository).clearCache(cacheFile)
            verify(repository).clearPreferences()
            verify(repository).wipeAll()
            verify(repository).deleteCurrentAccount()
            assertTrue(isFailure)
        }
    }

}