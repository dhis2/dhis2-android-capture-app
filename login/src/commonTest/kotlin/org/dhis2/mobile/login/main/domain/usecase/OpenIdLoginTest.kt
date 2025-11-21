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

class OpenIdLoginTest {
    private val repository: LoginRepository = mock()
    private lateinit var openIdLogin: OpenIdLogin

    private val serverUrl = "https://test.server.org"
    private val isNetworkAvailable = true
    private val clientId = "test-client-id"
    private val redirectUri = "dhis2://oauth"
    private val discoveryUri = "https://test.server.org/.well-known/openid-configuration"
    private val authorizationUri = "https://test.server.org/oauth/authorize"
    private val tokenUrl = "https://test.server.org/oauth/token"
    private val username = "openIdUser"

    @Before
    fun setUp() {
        openIdLogin = OpenIdLogin(repository)
    }

    @Test
    fun `GIVEN successful OpenID login with no existing accounts WHEN user logs in THEN biometric credentials are NOT deleted`() =
        runTest {
            // GIVEN - User has no other accounts (numberOfAccounts = 0)
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully with OpenID
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Login is successful
            assertIs<LoginResult.Success>(result)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
            verify(repository).numberOfAccounts()
            // Biometric credentials should NOT be deleted when numberOfAccounts < 1
            verify(repository, never()).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful OpenID login with one existing account WHEN user logs in to second account THEN biometric creds are deleted`() =
        runTest {
            // GIVEN - User has one existing account (numberOfAccounts = 1)
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 1
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully with OpenID to a second account
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Login is successful and biometric credentials are deleted
            assertIs<LoginResult.Success>(result)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
            verify(repository).numberOfAccounts()
            verify(repository).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful OpenID login with multiple existing accounts WHEN user logs in THEN biometric credentials are deleted`() =
        runTest {
            // GIVEN - User has multiple existing accounts (numberOfAccounts = 3)
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 3
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully with OpenID
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Login is successful and biometric credentials are deleted
            assertIs<LoginResult.Success>(result)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
            verify(repository).numberOfAccounts()
            verify(repository).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN failed OpenID login WHEN user attempts to log in THEN biometric credentials are NOT deleted`() =
        runTest {
            // GIVEN - OpenID login will fail
            val errorMessage = "OpenID authentication failed"
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.failure(Exception(errorMessage))

            // WHEN - User attempts to log in with OpenID
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Login fails and biometric credentials are NOT deleted
            assertIs<LoginResult.Error>(result)
            assertEquals(errorMessage, result.message)
            verify(repository, never()).getUsername()
            verify(repository, never()).unlockSession()
            verify(repository, never()).updateAvailableUsers(any())
            verify(repository, never()).updateServerUrls(any())
            verify(repository, never()).numberOfAccounts()
            verify(repository, never()).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful OpenID login with null discovery URI WHEN user logs in to second account THEN biometric creds are deleted`() =
        runTest {
            // GIVEN - User has one existing account and using null discoveryUri
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = null,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 1
            whenever(repository.displayTrackingMessage()) doReturn true
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn false

            // WHEN - User logs in successfully with OpenID
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = null,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Login is successful, biometric credentials are deleted, and tracking message is shown
            assertIs<LoginResult.Success>(result)
            assertEquals(true, result.displayTrackingMessage)
            assertEquals(false, result.initialSyncDone)
            verify(repository).unlockSession()
            verify(repository).updateAvailableUsers(username)
            verify(repository).updateServerUrls(serverUrl)
            verify(repository).numberOfAccounts()
            verify(repository).deleteBiometricCredentials()
        }

    @Test
    fun `GIVEN successful OpenID login WHEN repository operations are executed THEN they are called in correct order`() =
        runTest {
            // GIVEN
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 2
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN
            openIdLogin(
                serverUrl = serverUrl,
                isNetworkAvailable = isNetworkAvailable,
                clientId = clientId,
                redirectUri = redirectUri,
                discoveryUri = discoveryUri,
                authorizationUri = authorizationUri,
                tokenUrl = tokenUrl,
            )

            // THEN - Verify the order of operations
            val inOrder = org.mockito.kotlin.inOrder(repository)
            inOrder.verify(repository).loginWithOpenId(
                serverUrl = serverUrl,
                isNetworkAvailable = isNetworkAvailable,
                clientId = clientId,
                redirectUri = redirectUri,
                discoveryUri = discoveryUri,
                authorizationUri = authorizationUri,
                tokenUrl = tokenUrl,
            )
            inOrder.verify(repository).getUsername()
            inOrder.verify(repository).unlockSession()
            inOrder.verify(repository).updateAvailableUsers(username)
            inOrder.verify(repository).updateServerUrls(serverUrl)
            inOrder.verify(repository).numberOfAccounts()
            inOrder.verify(repository).deleteBiometricCredentials()
            inOrder.verify(repository).displayTrackingMessage()
            inOrder.verify(repository).initialSyncDone(serverUrl, username)
        }

    @Test
    fun `GIVEN network is offline WHEN user logs in with OpenID THEN login is attempted with offline flag`() =
        runTest {
            // GIVEN - Network is unavailable
            val isOffline = false
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isOffline,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User attempts to log in offline
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isOffline,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Login is successful with offline flag
            assertIs<LoginResult.Success>(result)
            verify(repository).loginWithOpenId(
                serverUrl = serverUrl,
                isNetworkAvailable = isOffline,
                clientId = clientId,
                redirectUri = redirectUri,
                discoveryUri = discoveryUri,
                authorizationUri = authorizationUri,
                tokenUrl = tokenUrl,
            )
        }

    @Test
    fun `GIVEN all success flags are true WHEN OpenID login succeeds THEN all flags are reflected in result`() =
        runTest {
            // GIVEN - Both tracking message and initial sync are required
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn true
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in successfully
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Both flags are true
            assertIs<LoginResult.Success>(result)
            assertEquals(true, result.displayTrackingMessage)
            assertEquals(true, result.initialSyncDone)
        }

    @Test
    fun `GIVEN all success flags are false WHEN OpenID login succeeds THEN all flags are false in result`() =
        runTest {
            // GIVEN - Neither tracking message nor initial sync are required
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn false

            // WHEN - User logs in successfully
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Both flags are false
            assertIs<LoginResult.Success>(result)
            assertEquals(false, result.displayTrackingMessage)
            assertEquals(false, result.initialSyncDone)
        }

    @Test
    fun `GIVEN custom redirect URI scheme WHEN OpenID login succeeds THEN custom scheme is handled`() =
        runTest {
            // GIVEN - Custom app redirect URI
            val customRedirectUri = "myapp://oauth/callback"
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = customRedirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in with custom redirect URI
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = customRedirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Login succeeds with custom redirect URI
            assertIs<LoginResult.Success>(result)
            verify(repository).loginWithOpenId(
                serverUrl = serverUrl,
                isNetworkAvailable = isNetworkAvailable,
                clientId = clientId,
                redirectUri = customRedirectUri,
                discoveryUri = discoveryUri,
                authorizationUri = authorizationUri,
                tokenUrl = tokenUrl,
            )
        }

    @Test
    fun `GIVEN different server URL formats WHEN OpenID login succeeds THEN server URL is used correctly`() =
        runTest {
            // GIVEN - Server URL with path
            val serverWithPath = "https://test.server.org/dhis"
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverWithPath,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverWithPath, username)) doReturn true

            // WHEN - User logs in with server URL containing path
            val result =
                openIdLogin(
                    serverUrl = serverWithPath,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Server URL is passed correctly
            assertIs<LoginResult.Success>(result)
            verify(repository).updateServerUrls(serverWithPath)
            verify(repository).initialSyncDone(serverWithPath, username)
        }

    @Test
    fun `GIVEN repository throws runtime exception WHEN OpenID login fails THEN exception message is returned`() =
        runTest {
            // GIVEN - Repository throws RuntimeException
            val runtimeError = "OpenID provider unavailable"
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.failure(RuntimeException(runtimeError))

            // WHEN - User attempts to log in
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Error message is propagated
            assertIs<LoginResult.Error>(result)
            assertEquals(runtimeError, result.message)
        }

    @Test
    fun `GIVEN successful OpenID login WHEN unlockSession is called THEN it is always executed`() =
        runTest {
            // GIVEN - Successful OpenID login
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn username
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, username)) doReturn true

            // WHEN - User logs in
            openIdLogin(
                serverUrl = serverUrl,
                isNetworkAvailable = isNetworkAvailable,
                clientId = clientId,
                redirectUri = redirectUri,
                discoveryUri = discoveryUri,
                authorizationUri = authorizationUri,
                tokenUrl = tokenUrl,
            )

            // THEN - Session is always unlocked on success
            verify(repository).unlockSession()
        }

    @Test
    fun `GIVEN failed OpenID login WHEN unlockSession would be called THEN it is never executed`() =
        runTest {
            // GIVEN - Failed OpenID login
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.failure(Exception("OpenID failed"))

            // WHEN - User attempts to log in
            openIdLogin(
                serverUrl = serverUrl,
                isNetworkAvailable = isNetworkAvailable,
                clientId = clientId,
                redirectUri = redirectUri,
                discoveryUri = discoveryUri,
                authorizationUri = authorizationUri,
                tokenUrl = tokenUrl,
            )

            // THEN - Session is never unlocked on failure
            verify(repository, never()).unlockSession()
        }

    @Test
    fun `GIVEN username with special characters WHEN OpenID login succeeds THEN username is handled correctly`() =
        runTest {
            // GIVEN - Username contains special characters
            val specialUsername = "user@example.com"
            whenever(
                repository.loginWithOpenId(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                ),
            ) doReturn Result.success(Unit)
            whenever(repository.getUsername()) doReturn specialUsername
            whenever(repository.numberOfAccounts()) doReturn 0
            whenever(repository.displayTrackingMessage()) doReturn false
            whenever(repository.initialSyncDone(serverUrl, specialUsername)) doReturn true

            // WHEN - User logs in successfully
            val result =
                openIdLogin(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkAvailable,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    authorizationUri = authorizationUri,
                    tokenUrl = tokenUrl,
                )

            // THEN - Special username is used correctly
            assertIs<LoginResult.Success>(result)
            verify(repository).updateAvailableUsers(specialUsername)
            verify(repository).initialSyncDone(serverUrl, specialUsername)
        }
}
