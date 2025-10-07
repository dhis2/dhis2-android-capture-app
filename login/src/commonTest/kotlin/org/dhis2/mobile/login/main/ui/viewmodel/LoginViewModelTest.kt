package org.dhis2.mobile.login.main.ui.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.state.ServerValidationUiState
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private val navigator: Navigator = mock()
    private val getInitialScreen: GetInitialScreen = mock()
    private val validateServer: ValidateServer = mock()
    private val appLinkNavigation: AppLinkNavigation = mock()
    private val testDispatcher = StandardTestDispatcher()
    private val mockAppLinkFlow = MutableSharedFlow<String>()
    private val networkStatusProvider: NetworkStatusProvider = mock()
    private val mockNetworkStatusFlow = MutableStateFlow(true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(appLinkNavigation.appLink).thenReturn(mockAppLinkFlow)
        whenever(networkStatusProvider.connectionStatus).thenReturn(mockNetworkStatusFlow)
    }

    @Test
    fun initialScreenIsSetCorrectlyWhenStarting() =
        runTest {
            val initialScreenState =
                LoginScreenState.ServerValidation(
                    currentServer = "https://test.dhis2.org",
                    availableServers = listOf("https://test.dhis2.org"),
                )

            whenever(getInitialScreen()).thenReturn(initialScreenState)

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    validateServer = validateServer,
                    appLinkNavigation = appLinkNavigation,
                    networkStatusProvider = networkStatusProvider,
                )

            advanceUntilIdle()

            verify(navigator).navigate(initialScreenState)
        }

    @Test
    fun serverValidationShowsErrorWhenValidationFails() =
        runTest {
            val serverUrl = "https://invalid-server.com"
            val errorMessage = "Server not found"

            whenever(getInitialScreen()).thenReturn(
                LoginScreenState.ServerValidation(
                    currentServer = "https://test.dhis2.org",
                    availableServers = listOf("https://test.dhis2.org"),
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
                    validateServer = validateServer,
                    appLinkNavigation = appLinkNavigation,
                    networkStatusProvider = networkStatusProvider,
                )

            viewModel.serverValidationState.test {
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
    fun cancelServerValidationStopsTheValidationJob() =
        runTest {
            whenever(getInitialScreen()).thenReturn(
                LoginScreenState.ServerValidation(
                    currentServer = "https://test.dhis2.org",
                    availableServers = listOf("https://test.dhis2.org"),
                ),
            )

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    validateServer = validateServer,
                    appLinkNavigation = appLinkNavigation,
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
    fun appLinkWithValidCodeIsHandledCorrectly() =
        runTest {
            val redirectUri = "https://vgarciabnz.github.io"
            val code = "auth_code_123"
            val appLinkUrl = "$redirectUri?code=$code&state=test"

            whenever(getInitialScreen()).thenReturn(
                LoginScreenState.ServerValidation(
                    currentServer = "https://test.dhis2.org",
                    availableServers = listOf("https://test.dhis2.org"),
                ),
            )

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    validateServer = validateServer,
                    appLinkNavigation = appLinkNavigation,
                    networkStatusProvider = networkStatusProvider,
                )

            viewModel.serverValidationState.test {
                assertEquals(ServerValidationUiState(), awaitItem())

                // Send app link
                mockAppLinkFlow.emit(appLinkUrl)

                // TODO: Verify that the code is processed correctly
                // Currently the LoginViewModel has a TODO comment for this functionality

                expectNoEvents()
            }
        }
}
