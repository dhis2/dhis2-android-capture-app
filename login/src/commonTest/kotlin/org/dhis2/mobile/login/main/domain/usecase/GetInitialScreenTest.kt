package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.junit.Before
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetInitialScreenTest {
    private val accountRepository: AccountRepository = mock()
    private val sessionRepository: SessionRepository = mock()
    private lateinit var getInitialScreen: GetInitialScreen

    @Before
    fun setUp() {
        getInitialScreen =
            GetInitialScreen(
                accountRepository = accountRepository,
                sessionRepository = sessionRepository,
            )
    }

    @Test
    fun `invoke returns ServerValidation when no accounts exist`() =
        runTest {
            // Given
            val availableServers = listOf("https://server1.com", "https://server2.com")
            whenever(accountRepository.getLoggedInAccounts()) doReturn emptyList()
            whenever(accountRepository.availableServers()) doReturn availableServers

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.ServerValidation>(result)
            assertEquals("", result.currentServer)
            assertEquals(availableServers, result.availableServers)
            assertEquals(false, result.hasAccounts)
        }

    @Test
    fun `invoke returns ServerValidation with empty server list when no accounts exist`() =
        runTest {
            // Given
            whenever(accountRepository.getLoggedInAccounts()) doReturn emptyList()
            whenever(accountRepository.availableServers()) doReturn emptyList()

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.ServerValidation>(result)
            assertEquals(emptyList(), result.availableServers)
        }

    @Test
    fun `invoke returns OauthLogin when single account with OAuth is enabled`() =
        runTest {
            // Given
            val account =
                createAccountModel(
                    name = "john",
                    serverUrl = "https://server.com",
                    isOauthEnabled = true,
                )
            whenever(accountRepository.getLoggedInAccounts()) doReturn listOf(account)

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.OauthLogin>(result)
            assertEquals("https://server.com", result.selectedServer)
        }

    @Test
    fun `invoke returns LegacyLogin when single account with OAuth disabled`() =
        runTest {
            // Given
            val account =
                createAccountModel(
                    name = "john",
                    serverUrl = "https://server.com",
                    serverName = "My Server",
                    isOauthEnabled = false,
                )
            whenever(accountRepository.getLoggedInAccounts()) doReturn listOf(account)

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.LegacyLogin>(result)
            assertEquals("https://server.com", result.selectedServer)
            assertEquals("john", result.selectedUsername)
            assertEquals("My Server", result.serverName)
        }

    @Test
    fun `invoke returns LegacyLogin with correct account details`() =
        runTest {
            // Given
            val account =
                createAccountModel(
                    name = "testuser",
                    serverUrl = "https://dhis2.example.com",
                    serverName = "DHIS2 Instance",
                    serverFlag = "flag_url",
                    allowRecovery = true,
                    isOauthEnabled = false,
                )
            whenever(accountRepository.getLoggedInAccounts()) doReturn listOf(account)

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.LegacyLogin>(result)
            assertEquals("https://dhis2.example.com", result.selectedServer)
            assertEquals("testuser", result.selectedUsername)
            assertEquals("DHIS2 Instance", result.serverName)
            assertEquals("flag_url", result.selectedServerFlag)
            assertEquals(true, result.allowRecovery)
        }

    @Test
    fun `invoke returns Accounts when multiple accounts and session not locked`() =
        runTest {
            // Given
            val accounts =
                listOf(
                    createAccountModel(name = "user1"),
                    createAccountModel(name = "user2"),
                )
            whenever(accountRepository.getLoggedInAccounts()) doReturn accounts
            whenever(sessionRepository.isSessionLocked()) doReturn false

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.Accounts>(result)
        }

    @Test
    fun `invoke returns OauthLogin when multiple accounts and session locked with OAuth account`() =
        runTest {
            // Given
            val activeAccount =
                createAccountModel(
                    name = "active_user",
                    serverUrl = "https://active.com",
                    isOauthEnabled = true,
                )
            val accounts =
                listOf(
                    createAccountModel(name = "user1"),
                    createAccountModel(name = "user2"),
                )
            whenever(accountRepository.getLoggedInAccounts()) doReturn accounts
            whenever(accountRepository.getActiveAccount()) doReturn activeAccount
            whenever(sessionRepository.isSessionLocked()) doReturn true

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.OauthLogin>(result)
            assertEquals("https://active.com", result.selectedServer)
        }

    @Test
    fun `invoke returns LegacyLogin when multiple accounts and session locked with legacy account`() =
        runTest {
            // Given
            val activeAccount =
                createAccountModel(
                    name = "active_user",
                    serverUrl = "https://active.com",
                    serverName = "Active Server",
                    isOauthEnabled = false,
                )
            val accounts =
                listOf(
                    createAccountModel(name = "user1"),
                    createAccountModel(name = "user2"),
                )
            whenever(accountRepository.getLoggedInAccounts()) doReturn accounts
            whenever(accountRepository.getActiveAccount()) doReturn activeAccount
            whenever(sessionRepository.isSessionLocked()) doReturn true

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.LegacyLogin>(result)
            assertEquals("https://active.com", result.selectedServer)
            assertEquals("active_user", result.selectedUsername)
        }

    @Test
    fun `invoke returns Accounts when multiple accounts and session locked but no active account`() =
        runTest {
            // Given
            val accounts =
                listOf(
                    createAccountModel(name = "user1"),
                    createAccountModel(name = "user2"),
                )
            whenever(accountRepository.getLoggedInAccounts()) doReturn accounts
            whenever(accountRepository.getActiveAccount()) doReturn null
            whenever(sessionRepository.isSessionLocked()) doReturn true

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.Accounts>(result)
        }

    @Test
    fun `invoke prioritizes session lock state over multiple accounts`() =
        runTest {
            // Given
            val activeAccount =
                createAccountModel(
                    name = "locked_user",
                    serverUrl = "https://locked.com",
                    isOauthEnabled = false,
                )
            val accounts =
                listOf(
                    createAccountModel(name = "user1"),
                    createAccountModel(name = "user2"),
                    createAccountModel(name = "user3"),
                )
            whenever(accountRepository.getLoggedInAccounts()) doReturn accounts
            whenever(accountRepository.getActiveAccount()) doReturn activeAccount
            whenever(sessionRepository.isSessionLocked()) doReturn true

            // When
            val result = getInitialScreen.invoke()

            // Then
            assertIs<LoginScreenState.LegacyLogin>(result)
            assertEquals("locked_user", result.selectedUsername)
        }

    @Test
    fun `invoke avoids redundant database calls for single account scenario`() =
        runTest {
            // Given
            val account = createAccountModel(name = "user", isOauthEnabled = true)
            whenever(accountRepository.getLoggedInAccounts()) doReturn listOf(account)

            // When
            getInitialScreen.invoke()

            // Then
            // Verify that getActiveAccount was not called (should only check session for multiple accounts)
            verify(accountRepository, never()).getActiveAccount()
        }

    // Helper functions
    private fun createAccountModel(
        name: String = "testuser",
        serverUrl: String = "https://test.com",
        serverName: String? = "Test Server",
        serverFlag: String? = null,
        allowRecovery: Boolean = false,
        isOauthEnabled: Boolean = false,
    ): AccountModel =
        AccountModel(
            name = name,
            serverUrl = serverUrl,
            serverName = serverName ?: "",
            serverDescription = null,
            serverFlag = serverFlag,
            allowRecovery = allowRecovery,
            oidcIcon = null,
            oidcLoginText = null,
            oidcUrl = null,
            isOauthEnabled = isOauthEnabled,
        )
}
