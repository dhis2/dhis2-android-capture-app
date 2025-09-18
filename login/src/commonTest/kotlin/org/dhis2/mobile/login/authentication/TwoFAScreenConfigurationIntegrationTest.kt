package org.dhis2.mobile.login.authentication

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.dhis2.mobile.login.authentication.domain.usecase.DisableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.EnableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.dhis2.mobile.login.authentication.ui.viewmodel.TwoFASettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFAScreenConfigurationIntegrationTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TwoFARepository
    private lateinit var getTwoFAStatus: GetTwoFAStatus
    private lateinit var disableTwoFA: DisableTwoFA
    private lateinit var enableTwoFA: EnableTwoFA
    private lateinit var mapper: TwoFAUiStateMapper
    private lateinit var viewModel: TwoFASettingsViewModel
    private val networkStatusProvider: NetworkStatusProvider = mock()
    private val dispatchers =
        Dispatcher(
            testDispatcher,
            testDispatcher,
            testDispatcher,
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        getTwoFAStatus = GetTwoFAStatus(repository)
        disableTwoFA = DisableTwoFA(repository)
        enableTwoFA = EnableTwoFA(repository)
        mapper = TwoFAUiStateMapper()
        whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given user taps on 2FA settings, When 2FA status is disabled, Then loading screen and enable 2FA screen are displayed`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()).thenReturn(
                TwoFAStatus.Disabled(
                    secretCode = "SECRETCODE",
                    errorMessage = null,
                ),
            )

            // When: 2FA status is checked
            viewModel =
                TwoFASettingsViewModel(
                    getTwoFAStatus = getTwoFAStatus,
                    enableTwoFA = enableTwoFA,
                    disableTwoFA = disableTwoFA,
                    mapper = mapper,
                    networkStatusProvider = networkStatusProvider,
                    dispatchers = dispatchers,
                )

            // Then: Loading screen is displayed followed by enable 2FA screen
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(TwoFAUiState.Checking, awaitItem())

                // Enable 2FA screen is displayed
                assertTrue(awaitItem() is TwoFAUiState.Enable)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given user taps on 2FA settings, When 2FA status is enabled, Then loading screen and disable 2FA screen are displayed`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()).thenReturn(
                TwoFAStatus.Enabled(),
            )

            // When: 2FA status is checked
            viewModel =
                TwoFASettingsViewModel(
                    getTwoFAStatus = getTwoFAStatus,
                    enableTwoFA = enableTwoFA,
                    disableTwoFA = disableTwoFA,
                    mapper = mapper,
                    networkStatusProvider = networkStatusProvider,
                    dispatchers = dispatchers,
                )

            // Then: Loading screen is displayed followed by disable 2FA screen
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(TwoFAUiState.Checking, awaitItem())

                // Disable 2FA screen is displayed
                assertTrue(awaitItem() is TwoFAUiState.Disable)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given user taps on 2FA settings, When 2FA status has no internet, Then loading screen and no connection screen are displayed`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()).thenReturn(
                TwoFAStatus.NoConnection,
            )

            // When: 2FA status is checked
            viewModel =
                TwoFASettingsViewModel(
                    getTwoFAStatus = getTwoFAStatus,
                    enableTwoFA = enableTwoFA,
                    disableTwoFA = disableTwoFA,
                    mapper = mapper,
                    networkStatusProvider = networkStatusProvider,
                    dispatchers = dispatchers,
                )

            // Then: Loading screen is displayed followed by no connection screen
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(TwoFAUiState.Checking, awaitItem())

                // No connection screen is displayed
                assertEquals(TwoFAUiState.NoConnection, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given 2FA enabled, When correct code is entered, Then enable 2FA screen is displayed`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()) doReturnConsecutively
                listOf(
                    TwoFAStatus.Enabled(),
                    TwoFAStatus.Disabled("123456"),
                )
            viewModel =
                TwoFASettingsViewModel(
                    getTwoFAStatus = getTwoFAStatus,
                    enableTwoFA = enableTwoFA,
                    disableTwoFA = disableTwoFA,
                    mapper = mapper,
                    networkStatusProvider = networkStatusProvider,
                    dispatchers = dispatchers,
                )

            // When: 2FA code is entered correctly"
            whenever(repository.disableTwoFAs("123456", true)) doReturn Result.success(Unit)

            // Then: enable 2FA screen is displayed after disable
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(TwoFAUiState.Checking, awaitItem())

                // Disable 2FA screen is displayed
                assertEquals(
                    TwoFAUiState.Disable(
                        state = InputShellState.UNFOCUSED,
                        isDisabling = false,
                        disableErrorMessage = null,
                    ),
                    awaitItem(),
                )

                viewModel.disableTwoFA("123456")

                assertEquals(
                    TwoFAUiState.Disable(
                        state = InputShellState.UNFOCUSED,
                        isDisabling = true,
                    ),
                    awaitItem(),
                )

                // Enable 2FA screen is displayed
                assertEquals(
                    TwoFAUiState.Enable(
                        secretCode = "123456",
                        isEnabling = false,
                        enableErrorMessage = null,
                    ),
                    awaitItem(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given 2FA enabled, When incorrect code, Then disable screen is displayed with error`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()).thenReturn(
                TwoFAStatus.Enabled(),
            )
            viewModel =
                TwoFASettingsViewModel(
                    getTwoFAStatus = getTwoFAStatus,
                    enableTwoFA = enableTwoFA,
                    disableTwoFA = disableTwoFA,
                    mapper = mapper,
                    networkStatusProvider = networkStatusProvider,
                    dispatchers = dispatchers,
                )

            // When: 2FA incorrect code is entered"
            whenever(repository.disableTwoFAs("123456", true)).thenReturn(
                Result.failure(Exception("error")),
            )

            // Then: disable 2FA screen is displayed with error
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(TwoFAUiState.Checking, awaitItem())

                // Disable 2FA screen is displayed
                assertTrue(awaitItem() is TwoFAUiState.Disable)

                viewModel.disableTwoFA("123456")
                assertEquals(
                    TwoFAUiState.Disable(
                        state = InputShellState.UNFOCUSED,
                        isDisabling = true,
                        disableErrorMessage = null,
                    ),
                    awaitItem(),
                )

                // Disable 2FA screen is displayed with error
                assertEquals(
                    TwoFAUiState.Disable(
                        state = InputShellState.ERROR,
                        isDisabling = false,
                        disableErrorMessage = "error",
                    ),
                    awaitItem(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given user is in enable 2FA, when 2FA is enabled, go to disable screen`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()) doReturnConsecutively
                listOf(
                    TwoFAStatus.Disabled(
                        secretCode = "123456",
                    ),
                    TwoFAStatus.Enabled(),
                )

            whenever(repository.enableTwoFA("123456", true)) doReturn Result.success(Unit)

            // When: 2FA status is checked
            viewModel =
                TwoFASettingsViewModel(
                    getTwoFAStatus = getTwoFAStatus,
                    enableTwoFA = enableTwoFA,
                    disableTwoFA = disableTwoFA,
                    mapper = mapper,
                    networkStatusProvider = networkStatusProvider,
                    dispatchers = dispatchers,
                )

            // Then: Loading screen is displayed followed by no connection screen
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(
                    TwoFAUiState.Checking,
                    awaitItem(),
                )

                // Enable 2FA screen is displayed
                assertEquals(
                    TwoFAUiState.Enable(
                        secretCode = "123456",
                        isEnabling = false,
                        enableErrorMessage = null,
                        errorMessage = null,
                    ),
                    awaitItem(),
                )

                // User enables 2FA
                viewModel.enableTwoFA("123456")

                assertEquals(
                    TwoFAUiState.Enable(
                        secretCode = "123456",
                        isEnabling = true,
                        enableErrorMessage = null,
                        errorMessage = null,
                    ),
                    awaitItem(),
                )

                assertEquals(
                    TwoFAUiState.Disable(
                        state = InputShellState.UNFOCUSED,
                        isDisabling = false,
                        disableErrorMessage = null,
                        errorMessage = null,
                    ),
                    awaitItem(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }
}
