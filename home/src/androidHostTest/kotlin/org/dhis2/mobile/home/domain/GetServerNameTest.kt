package org.dhis2.mobile.home.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.home.data.HomeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer

class GetServerNameTest {
    private val homeRepository: HomeRepository = mock()
    private val getServerName = GetServerName(homeRepository)

    @Test
    fun `should return the server url of the current account`() =
        runTest {
            whenever(homeRepository.serverName()) doReturn "https://play.dhis2.org"

            val result = getServerName()

            assertEquals("https://play.dhis2.org", result.getOrNull())
        }

    @Test
    fun `should return an empty string if there is no current account`() =
        runTest {
            whenever(homeRepository.serverName()) doReturn null

            val result = getServerName()

            assertEquals("", result.getOrNull())
        }

    @Test
    fun `should return failure if repository throws a domain error`() =
        runTest {
            val exception = DomainError.DatabaseError("Error")
            given(homeRepository.serverName()) willAnswer { throw exception }

            val result = getServerName()

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }

    @Test
    fun `should return failure if repository throws an unexpected exception`() =
        runTest {
            val exception = IllegalStateException("Error")
            given(homeRepository.serverName()) willAnswer { throw exception }

            val result = getServerName()

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
}
