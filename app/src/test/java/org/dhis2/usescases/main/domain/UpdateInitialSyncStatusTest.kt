package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UpdateInitialSyncStatusTest {

    private val homeRepository: HomeRepository = mock()
    private lateinit var updateInitialSyncStatus: UpdateInitialSyncStatus

    @Before
    fun setUp() {
        updateInitialSyncStatus = UpdateInitialSyncStatus(homeRepository)
    }

    @Test
    fun `should call set initial sync done`() = runTest {
        val result = updateInitialSyncStatus()

        assertTrue(result.isSuccess)
        verify(homeRepository).setInitialSyncDone()
    }

    @Test
    fun `should return failure when repository fails`() = runTest {
        val exception = DomainError.DataBaseError("Error")
        given(homeRepository.setInitialSyncDone()).willAnswer {
            throw exception
        }

        val result = updateInitialSyncStatus()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
