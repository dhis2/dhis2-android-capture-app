package org.dhis2.mobile.login.main.ui.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ImportDatabase
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.state.DatabaseImportState
import org.dhis2.mobile.login.main.ui.state.ServerValidationUiState
import org.junit.Before
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private val navigator: Navigator = mock()
    private val getInitialScreen: GetInitialScreen = mock()
    private val importDatabase: ImportDatabase = mock()
    private val validateServer: ValidateServer = mock()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val networkStatusProvider: NetworkStatusProvider = mock()
    private val mockNetworkStatusFlow = MutableStateFlow(true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(networkStatusProvider.connectionStatus).thenReturn(mockNetworkStatusFlow)
    }

    @Test
    fun `initial screen is set correctly when starting`() =
        runTest {
            val initialScreenState =
                LoginScreenState.ServerValidation(
                    currentServer = "https://test.dhis2.org",
                    availableServers = listOf("https://test.dhis2.org"),
                    hasAccounts = false,
                )

            whenever(getInitialScreen()).thenReturn(initialScreenState)

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    importDatabase = importDatabase,
                    validateServer = validateServer,
                    networkStatusProvider = networkStatusProvider,
                )

            verify(navigator).navigate(
                eq(initialScreenState),
                any(),
            )
        }

    @Test
    fun `server validation shows error when validation fails`() =
        runTest {
            val serverUrl = "https://invalid-server.com"
            val errorMessage = "Server not found"

            whenever(getInitialScreen()).thenReturn(
                LoginScreenState.ServerValidation(
                    currentServer = "https://test.dhis2.org",
                    availableServers = listOf("https://test.dhis2.org"),
                    hasAccounts = false,
                ),
            )
            whenever(validateServer(serverUrl, true)).thenReturn(
                ServerValidationResult.Error(
                    errorMessage,
                ),
            )

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    importDatabase = importDatabase,
                    validateServer = validateServer,
                    networkStatusProvider = networkStatusProvider,
                )

            viewModel.serverValidationState.test(timeout = 5.seconds) {
                assertEquals(ServerValidationUiState(), awaitItem())

                viewModel.onValidateServer(serverUrl)

                // Should show validation running
                val validationRunningState = awaitItem()

                assertEquals(true, validationRunningState.validationRunning)
                assertEquals(serverUrl, validationRunningState.currentServer)
                assertNull(validationRunningState.error)

                // Should show error and stop validation

                val errorState = awaitItem()
                assertEquals(errorMessage, errorState.error)
                assertEquals(serverUrl, errorState.currentServer)
                assertEquals(false, errorState.validationRunning)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `cancel server validation stops the validation job`() =
        runTest {
            whenever(getInitialScreen()).thenReturn(
                LoginScreenState.ServerValidation(
                    currentServer = "https://test.dhis2.org",
                    availableServers = listOf("https://test.dhis2.org"),
                    hasAccounts = false,
                ),
            )

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    importDatabase = importDatabase,
                    validateServer = validateServer,
                    networkStatusProvider = networkStatusProvider,
                )

            viewModel.serverValidationState.test {
                assertEquals(ServerValidationUiState(), awaitItem())

                viewModel.onValidateServer("any")

                val validationRunningState = awaitItem()
                assertEquals(true, validationRunningState.validationRunning)

                viewModel.cancelServerValidation()

                val updatedState = awaitItem()
                assertEquals(false, updatedState.validationRunning)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `successfully import database`() =
        runTest {
            whenever(importDatabase("path")).thenReturn(
                Result.success(Unit),
            )

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    importDatabase = importDatabase,
                    validateServer = validateServer,
                    networkStatusProvider = networkStatusProvider,
                )

            viewModel.importDatabaseState.test {
                assertEquals(null, awaitItem())

                viewModel.importDb("path")

                val importState = awaitItem()
                assertEquals(true, importState is DatabaseImportState.OnSuccess)
            }
        }

    @Test
    fun `failed import database`() =
        runTest {
            whenever(importDatabase("path")).thenReturn(
                Result.failure(Exception("Database already exists")),
            )

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    importDatabase = importDatabase,
                    validateServer = validateServer,
                    networkStatusProvider = networkStatusProvider,
                )

            viewModel.importDatabaseState.test {
                assertEquals(null, awaitItem())

                viewModel.importDb("path")

                val importState = awaitItem()
                assertEquals(true, importState is DatabaseImportState.OnFailure)
                assertEquals(
                    "Database already exists",
                    (importState as DatabaseImportState.OnFailure).message,
                )
            }
        }
}
