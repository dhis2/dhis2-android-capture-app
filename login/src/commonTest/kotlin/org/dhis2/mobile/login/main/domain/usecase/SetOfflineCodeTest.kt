package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.main.data.LoginRepository
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

class SetOfflineCodeTest {
    private val repository: LoginRepository = mock()
    private val setOfflineCode = SetOfflineCode(repository)

    private val code = "1234"

    @kotlin.test.Test
    fun `GIVEN a code WHEN invoked THEN it is stored through the repository`() =
        runTest {
            whenever(repository.setOfflineCode(code)) doReturn Result.success(Unit)

            val result = setOfflineCode(code)

            assertTrue(result.isSuccess)
            verify(repository).setOfflineCode(code)
        }

    @kotlin.test.Test
    fun `GIVEN the repository fails WHEN invoked THEN the failure is propagated`() =
        runTest {
            val error = Exception("cannot set code")
            whenever(repository.setOfflineCode(code)) doReturn Result.failure(error)

            val result = setOfflineCode(code)

            assertTrue(result.isFailure)
            verify(repository).setOfflineCode(code)
        }
}
