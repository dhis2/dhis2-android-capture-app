package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LogOutUserTest {
    private val workManagerController: WorkManagerController = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val filterManager: FilterManager = mock()
    private val homeRepository: HomeRepository = mock()
    private lateinit var logOutUser: LogOutUser

    @Before
    fun setUp() {
        logOutUser =
            LogOutUser(
                workManagerController = workManagerController,
                syncStatusController = syncStatusController,
                filterManager = filterManager,
                repository = homeRepository,
            )
    }

    @Test
    fun `should call all necessary methods on logout`() =
        runTest {
            whenever(homeRepository.accountsCount()) doReturn 1

            val result = logOutUser()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull())

            verify(workManagerController).cancelAllWork()
            verify(syncStatusController).restore()
            verify(filterManager).clearAllFilters()
            verify(homeRepository).clearPin()
            verify(homeRepository).logOut()
            verify(homeRepository).accountsCount()
        }

    @Test
    fun `should return failure if any method fails`() =
        runTest {
            val exception = DomainError.DataBaseError("Error")
            given(workManagerController.cancelAllWork()).willAnswer {
                throw exception
            }

            val result = logOutUser()

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
}
