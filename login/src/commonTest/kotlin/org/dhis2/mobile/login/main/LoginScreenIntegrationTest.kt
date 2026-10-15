package org.dhis2.mobile.login.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.accounts.domain.model.AuthorizationMethod
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.CredentialsEntryMode
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ImportDatabase
import org.dhis2.mobile.login.main.domain.usecase.ProcessDeviceEnrollment
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenIntegrationTest {
    private lateinit var viewModel: LoginViewModel
    private val navigator: Navigator = mock()
    private val accountRepository: AccountRepository = mock()
    private val sessionRepository: SessionRepository = mock()
    private val loginRepository: LoginRepository = mock()
    private val networkStatusProvider: NetworkStatusProvider = mock()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockNetworkStatusFlow = MutableStateFlow(true)
    private lateinit var getInitialScreen: GetInitialScreen
    private lateinit var importDatabase: ImportDatabase
    private lateinit var validateServer: ValidateServer
    private lateinit var processDeviceEnrollment: ProcessDeviceEnrollment

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(networkStatusProvider.connectionStatus).thenReturn(mockNetworkStatusFlow)

        importDatabase = ImportDatabase(repository = loginRepository)
        validateServer = ValidateServer(repository = loginRepository)
        getInitialScreen = GetInitialScreen(accountRepository, sessionRepository)
        processDeviceEnrollment = ProcessDeviceEnrollment(repository = loginRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     *
     * Test case: ANDROAPP-7220
     * Scenario: Manage accounts screen
     * Given the user is logged out
     * And has <number_of_accounts> accounts stored
     * When opens the app
     * Then goes to <destination_screen>
     *
     * Examples:
     * |number_of_accounts |destination_screen         |
     * | none              |server configuration screen|
     * | one               |existing account screen    |
     * | two or more       |manage accounts screen     |
     *
     */

    @Test
    fun `should navigate to server configuration screen when no accounts are stored`() =
        runTest {
            // Given the user is logged out and has no accounts stored
            whenever(accountRepository.getLoggedInAccounts()).thenReturn(emptyList())
            whenever(accountRepository.availableServers()).thenReturn(emptyList())
            whenever(sessionRepository.isSessionLocked()).thenReturn(false)

            // When opening the app
            initViewModel()

            // Then goes to server configuration screen
            verify(navigator).navigate(
                eq(
                    LoginScreenState.ServerValidation(
                        currentServer = "",
                        availableServers = emptyList(),
                        hasAccounts = false,
                    ),
                ),
                any(),
            )
        }

    @Test
    fun `should navigate to existing account screen when one legacy account is stored`() =
        runTest {
            // Given the user is logged out and has one legacy account stored
            val singleAccount =
                createLegacyAccount(
                    name = "testuser",
                    serverUrl = "https://test.dhis2.org",
                    serverName = "Test Server",
                )

            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(singleAccount))
            whenever(sessionRepository.isSessionLocked()).thenReturn(false)

            // When opening the app
            initViewModel()

            // Then goes to existing account screen (LegacyLogin)
            verify(navigator).navigate(
                eq(
                    LoginScreenState.LoginCredentials(
                        selectedServer = singleAccount.serverUrl,
                        selectedUsername = singleAccount.name,
                        serverName = singleAccount.serverName,
                        selectedServerFlag = singleAccount.serverFlag,
                        allowRecovery = singleAccount.allowRecovery,
                        entryMode = CredentialsEntryMode.EXISTING_PASSWORD,
                        autoPromptLogin = false,
                    ),
                ),
                any(),
            )
        }

    @Test
    fun `should navigate to OAuth login screen when one OAuth account is stored`() =
        runTest {
            // Given the user is logged out and has one OAuth account stored
            val oauthAccount =
                createOauthAccount(
                    name = "oauthuser",
                    serverUrl = "https://oauth.dhis2.org",
                )

            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(oauthAccount))
            whenever(sessionRepository.isSessionLocked()).thenReturn(false)

            // When opening the app
            initViewModel()

            // Then goes to login screen with OAuth
            verify(navigator).navigate(
                eq(
                    LoginScreenState.LoginCredentials(
                        selectedServer = oauthAccount.serverUrl,
                        selectedUsername = oauthAccount.name,
                        serverName = oauthAccount.serverName,
                        selectedServerFlag = oauthAccount.serverFlag,
                        allowRecovery = false,
                        entryMode = CredentialsEntryMode.EXISTING_OAUTH,
                        // Initial landing: the offline-credential dialog is not auto-presented.
                        autoPromptLogin = false,
                    ),
                ),
                any(),
            )
        }

    @Test
    fun `should navigate to manage accounts screen when two accounts are stored`() =
        runTest {
            // Given the user is logged out and has two accounts stored
            val account1 =
                createLegacyAccount(
                    name = "user1",
                    serverUrl = "https://server1.dhis2.org",
                    serverName = "Server 1",
                )
            val account2 =
                createLegacyAccount(
                    name = "user2",
                    serverUrl = "https://server2.dhis2.org",
                    serverName = "Server 2",
                )

            whenever(accountRepository.getLoggedInAccounts()).thenReturn(listOf(account1, account2))
            whenever(sessionRepository.isSessionLocked()).thenReturn(false)

            // When opening the app
            initViewModel()

            // Then goes to manage accounts screen (Accounts)
            verify(navigator).navigate(
                eq(LoginScreenState.Accounts),
                any(),
            )
        }

    @Test
    fun `should navigate to manage accounts screen when more than two accounts are stored`() =
        runTest {
            // Given the user is logged out and has multiple accounts stored
            val accounts =
                listOf(
                    createLegacyAccount("user1", "https://server1.dhis2.org", "Server 1"),
                    createLegacyAccount("user2", "https://server2.dhis2.org", "Server 2"),
                    createLegacyAccount("user3", "https://server3.dhis2.org", "Server 3"),
                )

            whenever(accountRepository.getLoggedInAccounts()).thenReturn(accounts)
            whenever(sessionRepository.isSessionLocked()).thenReturn(false)

            // When opening the app
            initViewModel()

            // Then goes to manage accounts screen (Accounts)
            verify(navigator).navigate(
                eq(LoginScreenState.Accounts),
                any(),
            )
        }

    /**
     *
     * Test case: ANDROAPP-7709
     * Scenario: Login flow selection after server validation
     * Given the user is on the server configuration screen
     * When enters the URL of a server with OAuth <oauth_status>
     * Then goes to the credentials screen in <entry_mode> mode
     *
     * Examples:
     * |oauth_status |entry_mode        |
     * | enabled     |NEW_ACCOUNT_OAUTH |
     * | disabled    |NEW_ACCOUNT_BASIC |
     *
     */

    @Test
    fun `should start OAuth login flow when server has OAuth enabled`() =
        runTest {
            // Given the user is on the server configuration screen
            givenServerConfigurationScreen()
            whenever(loginRepository.validateServer(SERVER_URL, true))
                .thenReturn(serverValidationSuccess(oAuthEnabled = true))
            initViewModel()

            // When enters the URL of a server with OAuth enabled
            viewModel.onValidateServer(SERVER_URL)
            advanceUntilIdle()

            // Then goes to the credentials screen in OAuth mode
            verify(navigator).navigate(
                eq(newAccountCredentials(CredentialsEntryMode.NEW_ACCOUNT_OAUTH)),
                any(),
            )
        }

    @Test
    fun `should start legacy login flow when server has OAuth disabled`() =
        runTest {
            // Given the user is on the server configuration screen
            givenServerConfigurationScreen()
            whenever(loginRepository.validateServer(SERVER_URL, true))
                .thenReturn(serverValidationSuccess(oAuthEnabled = false))
            initViewModel()

            // When enters the URL of a server with OAuth disabled
            viewModel.onValidateServer(SERVER_URL)
            advanceUntilIdle()

            // Then goes to the credentials screen in username and password mode
            verify(navigator).navigate(
                eq(newAccountCredentials(CredentialsEntryMode.NEW_ACCOUNT_BASIC)),
                any(),
            )
        }

    private suspend fun givenServerConfigurationScreen() {
        whenever(accountRepository.getLoggedInAccounts()).thenReturn(emptyList())
        whenever(accountRepository.availableServers()).thenReturn(emptyList())
        whenever(sessionRepository.isSessionLocked()).thenReturn(false)
    }

    private fun serverValidationSuccess(oAuthEnabled: Boolean) =
        ServerValidationResult.Success(
            serverName = SERVER_NAME,
            serverDescription = null,
            countryFlag = SERVER_FLAG,
            allowRecovery = true,
            oidcIcon = null,
            oidcLoginText = null,
            oidcUrl = null,
            oAuthEnabled = oAuthEnabled,
        )

    private fun newAccountCredentials(entryMode: CredentialsEntryMode) =
        LoginScreenState.LoginCredentials(
            selectedServer = SERVER_URL,
            selectedUsername = null,
            serverName = SERVER_NAME,
            selectedServerFlag = SERVER_FLAG,
            allowRecovery = true,
            entryMode = entryMode,
        )

    private fun initViewModel() {
        viewModel =
            LoginViewModel(
                navigator = navigator,
                getInitialScreen = getInitialScreen,
                importDatabase = importDatabase,
                validateServer = validateServer,
                networkStatusProvider = networkStatusProvider,
                appLinkNavigation = AppLinkNavigation(),
            )
    }

    private fun createLegacyAccount(
        name: String,
        serverUrl: String,
        serverName: String,
    ): AccountModel =
        AccountModel(
            name = name,
            serverUrl = serverUrl,
            serverName = serverName,
            serverDescription = null,
            serverFlag = "🇺🇸",
            allowRecovery = true,
            oidcIcon = null,
            oidcLoginText = null,
            oidcUrl = null,
            isOauthEnabled = false,
            authorizationMethod = AuthorizationMethod.BASIC,
        )

    private fun createOauthAccount(
        name: String,
        serverUrl: String,
    ): AccountModel =
        AccountModel(
            name = name,
            serverUrl = serverUrl,
            serverName = "OAuth Server",
            serverDescription = null,
            serverFlag = "🇺🇸",
            allowRecovery = false,
            oidcIcon = "icon",
            oidcLoginText = "Login with OAuth",
            oidcUrl = "https://oauth.dhis2.org/auth",
            isOauthEnabled = true,
            authorizationMethod = AuthorizationMethod.OAUTH2,
        )

    private companion object {
        const val SERVER_URL = "https://oauth.dhis2.org"
        const val SERVER_NAME = "Test Server"
        const val SERVER_FLAG = "🇺🇸"
    }
}
