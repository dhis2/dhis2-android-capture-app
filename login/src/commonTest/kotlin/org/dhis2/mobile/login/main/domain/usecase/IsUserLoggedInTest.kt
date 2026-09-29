package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.main.data.LoginRepository
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsUserLoggedInTest {
    private val repository: LoginRepository = mock()
    private val isUserLoggedIn = IsUserLoggedIn(repository)

    @kotlin.test.Test
    fun `GIVEN a logged in user WHEN invoked THEN it returns true from the repository`() =
        runTest {
            whenever(repository.isUserLoggedIn()) doReturn Result.success(true)

            val result = isUserLoggedIn(Unit)

            assertTrue(result.getOrThrow())
            verify(repository).isUserLoggedIn()
        }

    @kotlin.test.Test
    fun `GIVEN no logged in user WHEN invoked THEN it returns false from the repository`() =
        runTest {
            whenever(repository.isUserLoggedIn()) doReturn Result.success(false)

            val result = isUserLoggedIn(Unit)

            assertFalse(result.getOrThrow())
            verify(repository).isUserLoggedIn()
        }

    @kotlin.test.Test
    fun `GIVEN the repository fails WHEN invoked THEN the failure is propagated`() =
        runTest {
            val error = Exception("cannot check session")
            whenever(repository.isUserLoggedIn()) doReturn Result.failure(error)

            val result = isUserLoggedIn(Unit)

            assertTrue(result.isFailure)
            verify(repository).isUserLoggedIn()
        }
}
