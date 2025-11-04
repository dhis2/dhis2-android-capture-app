package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.HomeItemData
import org.dhis2.usescases.main.data.HomeRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer

class CheckSingleNavigationTest {
    private val homeRepository: HomeRepository = mock()
    private lateinit var checkSingleNavigation: CheckSingleNavigation

    @Before
    fun setUp() {
        checkSingleNavigation = CheckSingleNavigation(homeRepository)
    }

    @Test
    fun `should return HomeDataItem if there is only one program`() =
        runTest {
            whenever(homeRepository.homeItemCount()) doReturn 1
            whenever(homeRepository.singleHomeItemData()) doReturn
                HomeItemData.EventProgram(
                    "eventUid",
                    "eventLabel",
                    true,
                )
            assertTrue(checkSingleNavigation().isSuccess)
        }

    @Test
    fun `should return failure if more than one program`() =
        runTest {
            whenever(homeRepository.homeItemCount()) doReturn 2
            with(checkSingleNavigation()) {
                verify(homeRepository, times(0)).singleHomeItemData()
                assertTrue(isFailure)
            }
        }

    @Test
    fun `should return failure if there are no programs`() =
        runTest {
            whenever(homeRepository.homeItemCount()) doReturn 0
            with(checkSingleNavigation()) {
                verify(homeRepository, times(0)).singleHomeItemData()
                assertTrue(isFailure)
            }
        }

    @Test
    fun `should return failure if there is a domain exception`() =
        runTest {
            given(homeRepository.homeItemCount()) willAnswer { throw DomainError.DataBaseError("Test") }
            with(checkSingleNavigation()) {
                assertTrue(isFailure)
                assertTrue(exceptionOrNull() is DomainError)
            }
        }
}
