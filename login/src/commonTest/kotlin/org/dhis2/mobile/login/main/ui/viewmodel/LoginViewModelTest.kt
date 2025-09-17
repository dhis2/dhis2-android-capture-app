package org.dhis2.mobile.login.main.ui.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private val navigator: Navigator = mock()
    private val getInitialScreen: GetInitialScreen = mock()
    private val validateServer: ValidateServer = mock()
    private val appLinkNavigation: AppLinkNavigation = mock()
    private val testDispatcher = StandardTestDispatcher()

    private val mockAppLinkFlow = MutableSharedFlow<String>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(appLinkNavigation.appLink).thenReturn(mockAppLinkFlow)
    }

    @Test
    fun `initial screen is set correctly when starting`() =
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
                )

            viewModel.currentScreen.test {
                assertEquals(LoginScreenState.Loading, awaitItem())
                assertEquals(initialScreenState, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `server validation shows error when validation fails`() =
        runTest {
            val initialScreenState =
                LoginScreenState.ServerValidation(
                    currentServer = "",
                    availableServers = emptyList(),
                )
            val serverUrl = "https://invalid-server.com"
            val errorMessage = "Server not found"

            whenever(getInitialScreen()).thenReturn(initialScreenState)
            whenever(validateServer(serverUrl)).thenReturn(ServerValidationResult.Error(errorMessage))

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    validateServer = validateServer,
                    appLinkNavigation = appLinkNavigation,
                )

            viewModel.currentScreen.test {
                assertEquals(LoginScreenState.Loading, awaitItem())
                assertEquals(initialScreenState, awaitItem())

                viewModel.onValidateServer(serverUrl)

                // Should show validation running
                val validationRunningState = awaitItem() as LoginScreenState.ServerValidation
                assertEquals(true, validationRunningState.validationRunning)

                // Should show error and stop validation
                val errorState = awaitItem() as LoginScreenState.ServerValidation
                assertEquals(errorMessage, errorState.error)
                assertEquals(serverUrl, errorState.currentServer)
                assertEquals(false, errorState.validationRunning)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `cancel server validation stops the validation job`() =
        runTest {
            val initialScreenState =
                LoginScreenState.ServerValidation(
                    currentServer = "",
                    availableServers = emptyList(),
                    validationRunning = true,
                )

            whenever(getInitialScreen()).thenReturn(initialScreenState)

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    validateServer = validateServer,
                    appLinkNavigation = appLinkNavigation,
                )

            viewModel.currentScreen.test {
                assertEquals(LoginScreenState.Loading, awaitItem())
                assertEquals(initialScreenState, awaitItem())

                viewModel.cancelServerValidation()

                val updatedState = awaitItem() as LoginScreenState.ServerValidation
                assertEquals(false, updatedState.validationRunning)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `app link with valid code is handled correctly`() =
        runTest {
            val initialScreenState =
                LoginScreenState.ServerValidation(
                    currentServer = "",
                    availableServers = emptyList(),
                )
            val redirectUri = "https://vgarciabnz.github.io"
            val code = "auth_code_123"
            val appLinkUrl = "$redirectUri?code=$code&state=test"

            whenever(getInitialScreen()).thenReturn(initialScreenState)

            viewModel =
                LoginViewModel(
                    navigator = navigator,
                    getInitialScreen = getInitialScreen,
                    validateServer = validateServer,
                    appLinkNavigation = appLinkNavigation,
                )

            viewModel.currentScreen.test {
                assertEquals(LoginScreenState.Loading, awaitItem())
                assertEquals(initialScreenState, awaitItem())

                // Send app link
                mockAppLinkFlow.emit(appLinkUrl)

                // TODO: Verify that the code is processed correctly
                // Currently the LoginViewModel has a TODO comment for this functionality

                expectNoEvents()
            }
        }
}
