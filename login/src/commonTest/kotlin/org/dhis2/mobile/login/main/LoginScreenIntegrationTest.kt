package org.dhis2.mobile.login.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ImportDatabase
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
    private val appLinkNavigation: AppLinkNavigation = mock()
    private val networkStatusProvider: NetworkStatusProvider = mock()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockAppLinkFlow = MutableSharedFlow<String>()
    private val mockNetworkStatusFlow = MutableStateFlow(true)

    private lateinit var getInitialScreen: GetInitialScreen

    private lateinit var importDatabase: ImportDatabase

    private lateinit var validateServer: ValidateServer

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(appLinkNavigation.appLink).thenReturn(mockAppLinkFlow)
        whenever(networkStatusProvider.connectionStatus).thenReturn(mockNetworkStatusFlow)

        importDatabase = ImportDatabase(repository = loginRepository)
        validateServer = ValidateServer(repository = loginRepository)
        getInitialScreen = GetInitialScreen(accountRepository, sessionRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     *
     * Test case: ANDROAPP-7220
     * Scenario: Manage accounts screen
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
                    LoginScreenState.LegacyLogin(
                        selectedServer = singleAccount.serverUrl,
                        selectedUsername = singleAccount.name,
                        serverName = singleAccount.serverName,
                        selectedServerFlag = singleAccount.serverFlag,
                        allowRecovery = singleAccount.allowRecovery,
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

            // Then goes to OAuth login screen
            verify(navigator).navigate(
                eq(LoginScreenState.OauthLogin(selectedServer = oauthAccount.serverUrl)),
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

    private fun initViewModel() {
        viewModel =
            LoginViewModel(
                navigator = navigator,
                getInitialScreen = getInitialScreen,
                importDatabase = importDatabase,
                validateServer = validateServer,
                appLinkNavigation = appLinkNavigation,
                networkStatusProvider = networkStatusProvider,
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
        )
}
