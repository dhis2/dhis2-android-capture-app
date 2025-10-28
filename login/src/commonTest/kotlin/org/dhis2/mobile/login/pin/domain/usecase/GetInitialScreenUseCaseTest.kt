package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetInitialScreenUseCaseTest {
    private lateinit var useCase: GetInitialScreen
    private val accountRepository: AccountRepository = mock()
    private val sessionRepository: SessionRepository = mock()

    @Before
    fun setUp() {
        useCase = GetInitialScreen(accountRepository, sessionRepository)
    }

    @Test
    fun `go to server validation if there is no logged account`() =
        runTest {
            // Given
            whenever(accountRepository.getLoggedInAccounts()) doReturn emptyList()
            whenever(accountRepository.availableServers()) doReturn emptyList()

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.ServerValidation)
        }

    @Test
    fun `go to login screen if there is 1 logged account`() =
        runTest {
            // Given
            whenever(accountRepository.getLoggedInAccounts()) doReturn
                listOf(createAccountModel())

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.LegacyLogin)
        }

    @Test
    fun `go to oauth if there is 1 logged account with oauth`() =
        runTest {
            // Given
            whenever(accountRepository.getLoggedInAccounts()) doReturn
                listOf(createAccountModel(isOauthEnabled = true))

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.OauthLogin)
        }

    @Test
    fun `go to account if there is some logged accounts`() =
        runTest {
            // Given
            whenever(accountRepository.getLoggedInAccounts()) doReturn
                listOf(createAccountModel(), createAccountModel(), createAccountModel())
            whenever(sessionRepository.isSessionLocked()) doReturn false

            // When
            val result = useCase()

            // Then
            assertTrue(result is LoginScreenState.Accounts)
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
            val result = useCase()

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
            val result = useCase()

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
            val result = useCase()

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
            val result = useCase()

            // Then
            assertIs<LoginScreenState.LegacyLogin>(result)
            assertEquals("locked_user", result.selectedUsername)
        }

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
