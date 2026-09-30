package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoginUserOfflineTest {
    private val repository: LoginRepository = mock()
    private lateinit var loginUserOffline: LoginUserOffline

    private val serverUrl = "https://test.server.org"
    private val username = "testUser"
    private val code = "1234"

    @Before
    fun setUp() {
        loginUserOffline = LoginUserOffline(repository)
    }

    @Test
    fun `GIVEN successful offline login WHEN user logs in THEN success is returned`() =
        runTest {
            // GIVEN
            whenever(repository.loginUser(serverUrl, username, code)) doReturn Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN
            val result = loginUserOffline(serverUrl, username, code)

            // THEN
            assertIs<LoginResult.Success>(result)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
        }

    @Test
    fun `GIVEN a non-authentication failure WHEN user logs in THEN error is returned without attempts tracking`() =
        runTest {
            // GIVEN - a failure unrelated to the PIN itself (e.g. network) must not consume an attempt
            val errorMessage = "Server unreachable"
            whenever(repository.loginUser(serverUrl, username, code)) doReturn
                Result.failure(DomainError.NetworkError(errorMessage))

            // WHEN
            val result = loginUserOffline(serverUrl, username, code)

            // THEN
            assertIs<LoginResult.Error>(result)
            assertEquals(errorMessage, result.message)
            assertEquals(null, result.attemptsLeft)
            verify(repository, never()).unlockSession()
        }

    @Test
    fun `GIVEN a wrong PIN WHEN the first attempt fails THEN error reports two attempts left`() =
        runTest {
            // GIVEN
            val errorMessage = "Invalid PIN"
            whenever(repository.loginUser(serverUrl, username, code)) doReturn
                Result.failure(DomainError.AuthenticationError(errorMessage))

            // WHEN
            val result = loginUserOffline(serverUrl, username, code)

            // THEN
            assertIs<LoginResult.Error>(result)
            assertEquals(errorMessage, result.message)
            assertEquals(2, result.attemptsLeft)
        }

    @Test
    fun `GIVEN a wrong PIN WHEN two consecutive attempts fail THEN attempts left decreases each time`() =
        runTest {
            // GIVEN
            val errorMessage = "Invalid PIN"
            whenever(repository.loginUser(serverUrl, username, code)) doReturn
                Result.failure(DomainError.AuthenticationError(errorMessage))

            // WHEN - two consecutive wrong-PIN attempts
            val firstResult = loginUserOffline(serverUrl, username, code)
            val secondResult = loginUserOffline(serverUrl, username, code)

            // THEN
            assertIs<LoginResult.Error>(firstResult)
            assertEquals(2, firstResult.attemptsLeft)
            assertIs<LoginResult.Error>(secondResult)
            assertEquals(1, secondResult.attemptsLeft)
        }

    @Test
    fun `GIVEN a wrong PIN WHEN the third consecutive attempt fails THEN the account is locked out`() =
        runTest {
            // GIVEN
            val errorMessage = "Invalid PIN"
            whenever(repository.loginUser(serverUrl, username, code)) doReturn
                Result.failure(DomainError.AuthenticationError(errorMessage))

            // WHEN - three consecutive wrong-PIN attempts
            loginUserOffline(serverUrl, username, code)
            loginUserOffline(serverUrl, username, code)
            val thirdResult = loginUserOffline(serverUrl, username, code)

            // THEN - the third attempt triggers a lockout instead of another error
            assertIs<LoginResult.LockOut>(thirdResult)
            assertEquals(60, thirdResult.lockoutSeconds)
        }

    @Test
    fun `GIVEN a lockout was triggered WHEN the user fails again THEN the attempt counter restarted from zero`() =
        runTest {
            // GIVEN - a lockout was already triggered by three consecutive failures
            val errorMessage = "Invalid PIN"
            whenever(repository.loginUser(serverUrl, username, code)) doReturn
                Result.failure(DomainError.AuthenticationError(errorMessage))
            loginUserOffline(serverUrl, username, code)
            loginUserOffline(serverUrl, username, code)
            loginUserOffline(serverUrl, username, code)

            // WHEN - the user fails once more after the lockout
            val result = loginUserOffline(serverUrl, username, code)

            // THEN - the counter restarted, so this is treated as a first failed attempt again
            assertIs<LoginResult.Error>(result)
            assertEquals(2, result.attemptsLeft)
        }
}
