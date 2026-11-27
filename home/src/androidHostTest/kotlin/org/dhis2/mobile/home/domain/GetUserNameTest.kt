package org.dhis2.mobile.home.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.home.data.HomeRepository
import org.dhis2.mobile.home.domain.model.HomeUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer

class GetUserNameTest {
    private val homeRepository: HomeRepository = mock()
    private lateinit var getUserName: GetUserName

    @Before
    fun setUp() {
        getUserName = GetUserName(homeRepository)
    }

    @Test
    fun `should return full name if user exists`() =
        runTest {
            val user = HomeUser(firstName = "Peter", surname = "Jones", null)
            whenever(homeRepository.user()) doReturn user

            val result = getUserName()

            assertTrue(result.isSuccess)
            assertEquals("Peter Jones", result.getOrNull())
        }

    @Test
    fun `should return surname if first name is null`() =
        runTest {
            val user = HomeUser(firstName = null, surname = "Jones", null)
            whenever(homeRepository.user()) doReturn user

            val result = getUserName()

            assertTrue(result.isSuccess)
            assertEquals("Jones", result.getOrNull())
        }

    @Test
    fun `should return first name if surname is null`() =
        runTest {
            val user = HomeUser(firstName = "Peter", surname = null, null)
            whenever(homeRepository.user()) doReturn user

            val result = getUserName()

            assertTrue(result.isSuccess)
            assertEquals("Peter", result.getOrNull())
        }

    @Test
    fun `should return empty string if user is null`() =
        runTest {
            whenever(homeRepository.user()) doReturn null

            val result = getUserName()

            assertTrue(result.isSuccess)
            assertEquals("", result.getOrNull())
        }

    @Test
    fun `should return failure if repository throws error`() =
        runTest {
            val exception = DomainError.DatabaseError("Error")
            given(homeRepository.user()) willAnswer {
                throw exception
            }

            val result = getUserName()

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
}
