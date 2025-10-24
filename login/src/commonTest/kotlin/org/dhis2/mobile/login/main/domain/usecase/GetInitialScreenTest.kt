package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetInitialScreenTest {
    private lateinit var useCase: GetInitialScreen
    private val accountRepository: AccountRepository = mock()
    private val sessionRepository: SessionRepository = mock()

    @Before
    fun setUp() {
        useCase = GetInitialScreen(accountRepository, sessionRepository)
    }

    @Test
    fun `invoke returns ServerValidation when no accounts exist`() =
        runTest {
            // Given
            val availableServers = listOf("https://server1.com", "https://server2.com")
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(emptyList())
            whenever(accountRepository.availableServers()).thenReturn(availableServers)

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.ServerValidation)
            assertEquals("", result.currentServer)
            assertEquals(availableServers, result.availableServers)
            assertEquals(false, result.hasAccounts)
        }

    @Test
    fun `invoke returns LegacyLogin when single account exists with OAuth disabled`() =
        runTest {
            // Given
            val account = AccountModel(
                name = "testuser",
                serverUrl = "https://test.dhis2.org",
                serverName = "Test Server",
                serverDescription = "Test Description",
                serverFlag = "test_flag",
                allowRecovery = true,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(account))

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.LegacyLogin)
            assertEquals("https://test.dhis2.org", result.selectedServer)
            assertEquals("testuser", result.selectedUsername)
            assertEquals("Test Server", result.serverName)
            assertEquals("test_flag", result.selectedServerFlag)
            assertEquals(true, result.allowRecovery)
        }

    @Test
    fun `invoke returns OauthLogin when single account exists with OAuth enabled`() =
        runTest {
            // Given
            val account = AccountModel(
                name = "oauthuser",
                serverUrl = "https://oauth.dhis2.org",
                serverName = "OAuth Server",
                serverDescription = "OAuth Description",
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = "icon_url",
                oidcLoginText = "Login with OAuth",
                oidcUrl = "https://oauth.provider.com",
                isOauthEnabled = true,
            )
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(account))

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.OauthLogin)
            assertEquals("https://oauth.dhis2.org", result.selectedServer)
        }

    @Test
    fun `invoke returns Accounts when multiple accounts exist and session is not locked`() =
        runTest {
            // Given
            val account1 = AccountModel(
                name = "user1",
                serverUrl = "https://server1.dhis2.org",
                serverName = "Server 1",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            val account2 = AccountModel(
                name = "user2",
                serverUrl = "https://server2.dhis2.org",
                serverName = "Server 2",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(account1, account2))
            whenever(sessionRepository.isSessionLocked()).thenReturn(false)

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.Accounts)
        }

    @Test
    fun `invoke returns OauthLogin when session is locked and active account has OAuth enabled`() =
        runTest {
            // Given
            val account1 = AccountModel(
                name = "user1",
                serverUrl = "https://server1.dhis2.org",
                serverName = "Server 1",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            val account2 = AccountModel(
                name = "user2",
                serverUrl = "https://server2.dhis2.org",
                serverName = "Server 2",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            val activeAccount = AccountModel(
                name = "activeuser",
                serverUrl = "https://active.dhis2.org",
                serverName = "Active Server",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = "icon",
                oidcLoginText = "Login",
                oidcUrl = "https://oauth.com",
                isOauthEnabled = true,
            )
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(account1, account2))
            whenever(sessionRepository.isSessionLocked()).thenReturn(true)
            whenever(accountRepository.getActiveAccount()).thenReturn(activeAccount)

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.OauthLogin)
            assertEquals("https://active.dhis2.org", result.selectedServer)
        }

    @Test
    fun `invoke returns LegacyLogin when session is locked and active account has OAuth disabled`() =
        runTest {
            // Given
            val account1 = AccountModel(
                name = "user1",
                serverUrl = "https://server1.dhis2.org",
                serverName = "Server 1",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            val account2 = AccountModel(
                name = "user2",
                serverUrl = "https://server2.dhis2.org",
                serverName = "Server 2",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            val activeAccount = AccountModel(
                name = "activeuser",
                serverUrl = "https://active.dhis2.org",
                serverName = "Active Server",
                serverDescription = "Active Description",
                serverFlag = "active_flag",
                allowRecovery = true,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(account1, account2))
            whenever(sessionRepository.isSessionLocked()).thenReturn(true)
            whenever(accountRepository.getActiveAccount()).thenReturn(activeAccount)

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.LegacyLogin)
            assertEquals("https://active.dhis2.org", result.selectedServer)
            assertEquals("activeuser", result.selectedUsername)
            assertEquals("Active Server", result.serverName)
            assertEquals("active_flag", result.selectedServerFlag)
            assertEquals(true, result.allowRecovery)
        }

    @Test
    fun `invoke returns Accounts when session is locked but no active account exists`() =
        runTest {
            // Given
            val account1 = AccountModel(
                name = "user1",
                serverUrl = "https://server1.dhis2.org",
                serverName = "Server 1",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            val account2 = AccountModel(
                name = "user2",
                serverUrl = "https://server2.dhis2.org",
                serverName = "Server 2",
                serverDescription = null,
                serverFlag = null,
                allowRecovery = false,
                oidcIcon = null,
                oidcLoginText = null,
                oidcUrl = null,
                isOauthEnabled = false,
            )
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(account1, account2))
            whenever(sessionRepository.isSessionLocked()).thenReturn(true)
            whenever(accountRepository.getActiveAccount()).thenReturn(null)

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.Accounts)
        }

    @Test
    fun `invoke returns ServerValidation with empty current server when no accounts`() =
        runTest {
            // Given
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(emptyList())
            whenever(accountRepository.availableServers()).thenReturn(emptyList())

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.ServerValidation)
            assertEquals("", result.currentServer)
            assertEquals(emptyList(), result.availableServers)
            assertEquals(false, result.hasAccounts)
            assertEquals(null, result.error)
            assertEquals(false, result.validationRunning)
        }
}
