package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.main.data.LoginRepository
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

class SetOAuthPinTest {
    private val repository: LoginRepository = mock()
    private val setOAuthPin = SetOAuthPin(repository)

    private val pin = "1234"

    @kotlin.test.Test
    fun `GIVEN a pin WHEN invoked THEN it is stored through the repository`() =
        runTest {
            whenever(repository.setOfflinePin(pin)) doReturn Result.success(Unit)

            val result = setOAuthPin(pin)

            assertTrue(result.isSuccess)
            verify(repository).setOfflinePin(pin)
        }

    @kotlin.test.Test
    fun `GIVEN the repository fails WHEN invoked THEN the failure is propagated`() =
        runTest {
            val error = Exception("cannot set pin")
            whenever(repository.setOfflinePin(pin)) doReturn Result.failure(error)

            val result = setOAuthPin(pin)

            assertTrue(result.isFailure)
            verify(repository).setOfflinePin(pin)
        }
}
