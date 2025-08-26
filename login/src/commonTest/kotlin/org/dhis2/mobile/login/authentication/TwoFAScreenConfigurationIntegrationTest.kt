package org.dhis2.mobile.login.authentication

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.dhis2.mobile.login.authentication.domain.usecase.DisableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.EnableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFASecretCode
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.dhis2.mobile.login.authentication.ui.viewmodel.TwoFASettingsViewModel
import org.mockito.kotlin.doReturn
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
    private lateinit var getTwoFASecretCode: GetTwoFASecretCode
    private lateinit var disableTwoFA: DisableTwoFA
    private lateinit var enableTwoFA: EnableTwoFA
    private lateinit var mapper: TwoFAUiStateMapper
    private lateinit var viewModel: TwoFASettingsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        getTwoFAStatus = GetTwoFAStatus(repository)
        getTwoFASecretCode = GetTwoFASecretCode(repository)
        disableTwoFA = DisableTwoFA(repository)
        enableTwoFA = EnableTwoFA(repository)
        mapper = TwoFAUiStateMapper()
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
                TwoFAStatus.Disabled(),
            )

            // When: 2FA status is checked
            viewModel = TwoFASettingsViewModel(
                getTwoFAStatus,
                getTwoFASecretCode,
                enableTwoFA,
                disableTwoFA,
                mapper,
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
            viewModel = TwoFASettingsViewModel(
                getTwoFAStatus,
                getTwoFASecretCode,
                enableTwoFA,
                disableTwoFA,
                mapper,
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
            viewModel = TwoFASettingsViewModel(
                getTwoFAStatus,
                getTwoFASecretCode,
                enableTwoFA,
                disableTwoFA,
                mapper,
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
    fun `Given user in 2FA settings with 2FA status enabled, When correct code is entered and button clicked, Then enable 2FA screen is displayed`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()).thenReturn(
                TwoFAStatus.Enabled(),
            )
            viewModel = TwoFASettingsViewModel(
                getTwoFAStatus,
                getTwoFASecretCode,
                enableTwoFA,
                disableTwoFA,
                mapper,
            )

            // When: 2FA code is entered correctly"
            whenever(repository.disableTwoFAs("123456")).thenReturn(
                flowOf(TwoFAStatus.Disabled()),
            )
            viewModel.disableTwoFA("123456")

            // Then: enable 2FA screen is displayed after disable
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(TwoFAUiState.Checking, awaitItem())

                // Disable 2FA screen is displayed
                assertTrue(awaitItem() is TwoFAUiState.Disable)

                // Enable 2FA screen is displayed
                assertTrue(awaitItem() is TwoFAUiState.Enable)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given user in 2FA settings with 2FA status enabled, When incorrect code is entered and button clicked, Then disable 2FA screen is displayed with error`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()).thenReturn(
                TwoFAStatus.Enabled(),
            )
            viewModel = TwoFASettingsViewModel(
                getTwoFAStatus,
                getTwoFASecretCode,
                enableTwoFA,
                disableTwoFA,
                mapper,
            )

            // When: 2FA code is entered correctly"
            whenever(repository.disableTwoFAs("123456")).thenReturn(
                flowOf(TwoFAStatus.Enabled("error")),
            )
            viewModel.disableTwoFA("123456")

            // Then: enable 2FA screen is displayed after disable
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(TwoFAUiState.Checking, awaitItem())

                // Disable 2FA screen is displayed
                assertTrue(awaitItem() is TwoFAUiState.Disable)

                // Disable 2FA screen is displayed with error
                assertEquals(TwoFAUiState.Disable("error"), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given user is in enable 2FA, when 2FA is enabled, go to disable screen`() =
        runTest {
            // Given: User taps on 2FA settings
            whenever(repository.getTwoFAStatus()).thenReturn(
                TwoFAStatus.Disabled(),
            )

            whenever(repository.enableTwoFA("123456")) doReturn flowOf(true)

            // When: 2FA status is checked
            viewModel = TwoFASettingsViewModel(
                getTwoFAStatus = getTwoFAStatus,
                getTwoFASecretCode = getTwoFASecretCode,
                enableTwoFA = enableTwoFA,
                disableTwoFA = disableTwoFA,
                mapper = mapper,
            )

            // Then: Loading screen is displayed followed by no connection screen
            viewModel.uiState.test {
                // Loading screen is displayed
                assertEquals(TwoFAUiState.Checking, awaitItem())

                // Enable 2FA screen is displayed
                assertEquals(TwoFAUiState.Enable(), awaitItem())

                // User enables 2FA
                viewModel.enableTwoFA("123456")

                assertEquals(TwoFAUiState.Disable(), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
