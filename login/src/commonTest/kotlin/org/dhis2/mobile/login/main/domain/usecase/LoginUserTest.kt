package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoginUserTest {
    private val repository: LoginRepository = mock()
    private lateinit var loginUser: LoginUser

    private val serverUrl = "https://test.server.org"
    private val username = "testUser"
    private val password = "testPassword"
    private val isNetworkAvailable = true

    @Before
    fun setUp() {
        loginUser = LoginUser(repository)
    }

    @Test
    fun `GIVEN successful login with no existing accounts WHEN user logs in THEN biometric credentials are NOT deleted`() =
        runTest {
            // GIVEN - User has no other accounts (numberOfAccounts = 0)
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Login is successful
            assertIs<LoginResult.Success>(result)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
            verify(repository).numberOfAccounts()
            // Biometric credentials should NOT be deleted when numberOfAccounts < 2
            verify(repository, never()).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful login with one existing account WHEN user logs in to second account THEN biometric creds are NOT deleted`() =
        runTest {
            // GIVEN - User has one existing account (numberOfAccounts = 1)
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 1
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully to a second account
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Login is successful and biometric credentials are NOT deleted
            assertIs<LoginResult.Success>(result)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
            verify(repository).numberOfAccounts()
            // Biometric credentials should NOT be deleted when numberOfAccounts == 1 (only deleted when >= 2)
            verify(repository, never()).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful login with multiple existing accounts WHEN user logs in THEN biometric credentials are deleted`() =
        runTest {
            // GIVEN - User has multiple existing accounts (numberOfAccounts = 3)
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 3
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Login is successful and biometric credentials are deleted
            assertIs<LoginResult.Success>(result)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
            verify(repository).numberOfAccounts()
            verify(repository).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN failed login WHEN user attempts to log in THEN biometric credentials are NOT deleted`() =
        runTest {
            // GIVEN - Login will fail
            val errorMessage = "Invalid credentials"
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.failure(Exception(errorMessage))

            // WHEN - User attempts to log in
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Login fails and biometric credentials are NOT deleted
            assertIs<LoginResult.Error>(result)
            assertEquals(errorMessage, result.message)
            verify(repository, never()).unlockSession()
            verify(repository, never()).updateAvailableUsers(any())
            verify(repository, never()).updateServerUrls(any())
            verify(repository, never()).numberOfAccounts()
            verify(repository, never()).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful login WHEN user logs in to second account THEN biometric creds are NOT deleted but tracking message displayed`() =
        runTest {
            // GIVEN - User has one existing account and tracking message should be displayed
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 1
            whenever(repository.displayTrackingMessage()) doReturn true
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn false

            // WHEN - User logs in successfully
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Login is successful, biometric credentials are NOT deleted (only at >= 2), and tracking message is shown
            assertIs<LoginResult.Success>(result)
            assertEquals(true, result.displayTrackingMessage)
            assertEquals(false, result.initialSyncDone)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
            verify(repository).numberOfAccounts()
            verify(repository, never()).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful login exactly at threshold WHEN numberOfAccounts equals 2 THEN biometric credentials are deleted`() =
        runTest {
            // GIVEN - numberOfAccounts is exactly 2 (the threshold)
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 2
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            assertIs<LoginResult.Success>(result)
            verify(repository).numberOfAccounts()
            verify(repository).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful login WHEN repository operations are executed THEN they are called in correct order`() =
        runTest {
            // GIVEN
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 2
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN
            loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Verify the order of operations
            val inOrder = org.mockito.kotlin.inOrder(repository)
            inOrder.verify(repository).loginUser(serverUrl, username, password, isNetworkAvailable)
            inOrder.verify(repository).unlockSession()
            inOrder.verify(repository).updateAvailableUsers(username)
            inOrder.verify(repository).updateServerUrls(serverUrl)
            inOrder.verify(repository).numberOfAccounts()
            inOrder.verify(repository).deleteBiometricCredentials()
            inOrder.verify(repository).displayTrackingMessage()
            inOrder.verify(repository).initialSyncDone(serverUrl, username)
        }

    @Test
    fun `GIVEN network is offline WHEN user logs in THEN login is attempted with offline flag`() =
        runTest {
            // GIVEN - Network is unavailable
            val isOffline = true
            whenever(repository.loginUser(serverUrl, username, password, isOffline)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User attempts to log in offline
            val result = loginUser(serverUrl, username, password, isOffline)

            // THEN - Login is successful with offline flag
            assertIs<LoginResult.Success>(result)
            verify(repository).loginUser(serverUrl, username, password, isOffline)
        }

    @Test
    fun `GIVEN empty username WHEN user logs in THEN username is passed to repository`() =
        runTest {
            // GIVEN - Empty username
            val emptyUsername = ""
            whenever(repository.loginUser(serverUrl, emptyUsername, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, emptyUsername)) doReturn true

            // WHEN - Login with empty username
            val result = loginUser(serverUrl, emptyUsername, password, isNetworkAvailable)

            // THEN - Repository receives empty username
            assertIs<LoginResult.Success>(result)
            verify(repository).loginUser(serverUrl, emptyUsername, password, isNetworkAvailable)
            verify(repository).updateAvailableUsers(emptyUsername)
            verify(repository).initialSyncDone(serverUrl, emptyUsername)
        }

    @Test
    fun `GIVEN empty password WHEN user logs in THEN password is passed to repository`() =
        runTest {
            // GIVEN - Empty password
            val emptyPassword = ""
            whenever(repository.loginUser(serverUrl, username, emptyPassword, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - Login with empty password
            val result = loginUser(serverUrl, username, emptyPassword, isNetworkAvailable)

            // THEN - Repository receives empty password
            assertIs<LoginResult.Success>(result)
            verify(repository).loginUser(serverUrl, username, emptyPassword, isNetworkAvailable)
        }

    @Test
    fun `GIVEN different server URL formats WHEN user logs in THEN server URL is normalized correctly`() =
        runTest {
            // GIVEN - Server URL with trailing slash
            val serverWithSlash = "https://test.server.org/"
            whenever(repository.loginUser(serverWithSlash, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverWithSlash, username)) doReturn true

            // WHEN - User logs in
            val result = loginUser(serverWithSlash, username, password, isNetworkAvailable)

            // THEN - Server URL is passed as-is to repository
            assertIs<LoginResult.Success>(result)
            verify(repository).loginUser(serverWithSlash, username, password, isNetworkAvailable)
            verify(repository).updateServerUrls(serverWithSlash)
            verify(repository).initialSyncDone(serverWithSlash, username)
        }

    @Test
    fun `GIVEN all success flags are true WHEN user logs in THEN all flags are reflected in result`() =
        runTest {
            // GIVEN - Both tracking message and initial sync are required
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn true
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Both flags are true
            assertIs<LoginResult.Success>(result)
            assertEquals(true, result.displayTrackingMessage)
            assertEquals(true, result.initialSyncDone)
        }

    @Test
    fun `GIVEN all success flags are false WHEN user logs in THEN all flags are false in result`() =
        runTest {
            // GIVEN - Neither tracking message nor initial sync are required
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn false

            // WHEN - User logs in successfully
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Both flags are false
            assertIs<LoginResult.Success>(result)
            assertEquals(false, result.displayTrackingMessage)
            assertEquals(false, result.initialSyncDone)
        }

    @Test
    fun `GIVEN repository throws runtime exception WHEN login fails THEN exception message is returned`() =
        runTest {
            // GIVEN - Repository throws RuntimeException
            val runtimeError = "Database connection failed"
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.failure(RuntimeException(runtimeError))

            // WHEN - User attempts to log in
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Error message is propagated
            assertIs<LoginResult.Error>(result)
            assertEquals(runtimeError, result.message)
        }

    @Test
    fun `GIVEN successful login with initial sync not done WHEN user logs in THEN result reflects sync state`() =
        runTest {
            // GIVEN - Initial sync has not been completed
            whenever(repository.loginUser(serverUrl, username, password, isNetworkAvailable)) doReturn
                Result.success(Unit)
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn false

            // WHEN - User logs in successfully
            val result = loginUser(serverUrl, username, password, isNetworkAvailable)

            // THEN - Initial sync flag is false
            assertIs<LoginResult.Success>(result)
            assertEquals(false, result.initialSyncDone)
        }
}
