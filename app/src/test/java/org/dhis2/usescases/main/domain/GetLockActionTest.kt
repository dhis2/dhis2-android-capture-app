package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.dhis2.usescases.main.domain.model.LockAction
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer

class GetLockActionTest {
    private val homeRepository: HomeRepository = mock()
    private lateinit var getLockAction: GetLockAction

    @Before
    fun setUp() {
        getLockAction = GetLockAction(homeRepository)
    }

    @Test
    fun `should return BlockSession action if pin is set`() =
        runTest {
            whenever(homeRepository.isPinStored()) doReturn true
            val result = getLockAction()
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull() is LockAction.BlockSession)
        }

    @Test
    fun `should return CreatePin action if pin is set`() =
        runTest {
            whenever(homeRepository.isPinStored()) doReturn false
            val result = getLockAction()
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull() is LockAction.CreatePin)
        }

    @Test
    fun `should return failure if an exception is thrown`() =
        runTest {
            given(homeRepository.isPinStored()) willAnswer {
                throw DomainError.DataBaseError("Test")
            }
            val result = getLockAction()
            assertTrue(result.isFailure)
        }
}
