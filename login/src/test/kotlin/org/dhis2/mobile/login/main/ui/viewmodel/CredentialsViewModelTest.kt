
package org.dhis2.mobile.login.main.ui.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.BiometricsInfo
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.GetHasOtherAccounts
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.OpenIdLogin
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.states.LoginState
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.GetIsSessionLockedUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class CredentialsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val navigator: Navigator = mock()
    private val getAvailableUsernames: GetAvailableUsernames = mock()
    private val getBiometricInfo: GetBiometricInfo = mock()
    private val getHasOtherAccounts: GetHasOtherAccounts = mock()
    private val loginUser: LoginUser = mock()
    private val biometricLogin: BiometricLogin = mock()
    private val openIdLogin: OpenIdLogin = mock()
    private val updateTrackingPermission: UpdateTrackingPermission = mock()
    private val updateBiometricPermission: UpdateBiometricPermission = mock()
    private val networkStatusProvider: NetworkStatusProvider = mock()
    private val getIsSessionLockedUseCase: GetIsSessionLockedUseCase = mock()
    private val forgotPinUseCase: ForgotPinUseCase = mock()

    private lateinit var viewModel: CredentialsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a fresh view model WHEN data is loaded THEN initial state is correct`() =
        runTest {
            // GIVEN
            val usernames = listOf("user1", "user2")
            val serverUrl = "https://test.server.org"
            whenever(getAvailableUsernames()) doReturn usernames
            whenever(getBiometricInfo(serverUrl)) doReturn
                BiometricsInfo(
                    canUseBiometrics = true,
                    displayBiometricsMessageAfterLogin = false,
                )
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false

            // WHEN
            initViewModel(serverUrl = serverUrl)

            // THEN
            viewModel.credentialsScreenState.test {
                skipItems(1) // skip default value

                val initialState = awaitItem()
                assertFalse(initialState.hasOtherAccounts)
                assertTrue(initialState.canUseBiometrics)
                assertEquals(initialState.credentialsInfo.availableUsernames, usernames)
            }
        }

    @Test
    fun `GIVEN username and password are empty WHEN username is updated THEN login is disabled`() =
        runTest {
            // GIVEN
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn
                BiometricsInfo(
                    canUseBiometrics = false,
                    displayBiometricsMessageAfterLogin = false,
                )
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false

            initViewModel()

            viewModel.credentialsScreenState.test {
                skipItems(1) // Skip initial state

                // WHEN
                viewModel.updateUsername("test_user")

                // THEN
                val updatedState = awaitItem()
                assertEquals("test_user", updatedState.credentialsInfo.username)
                assertEquals(LoginState.Disabled, updatedState.loginState)
            }
        }

    @Test
    fun `GIVEN username is filled WHEN password is updated THEN login is enabled`() =
        runTest {
            // GIVEN
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn
                BiometricsInfo(
                    canUseBiometrics = false,
                    displayBiometricsMessageAfterLogin = false,
                )
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false

            initViewModel()

            viewModel.credentialsScreenState.test {
                skipItems(2) // Skip initial state

                viewModel.updateUsername("test_user")
                skipItems(1)

                // WHEN
                viewModel.updatePassword("test_password")

                // THEN
                val updatedState = awaitItem()
                assertEquals("test_password", updatedState.credentialsInfo.password)
                assertEquals(LoginState.Enabled, updatedState.loginState)
            }
        }

    @Test
    fun `GIVEN successful login WHEN login is clicked THEN state is updated`() =
        runTest {
            // GIVEN
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn
                BiometricsInfo(
                    canUseBiometrics = false,
                    displayBiometricsMessageAfterLogin = false,
                )
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel()

            viewModel.credentialsScreenState.test(timeout = 5.seconds) {
                skipItems(2)
                viewModel.updateUsername("user")
                skipItems(1)
                viewModel.updatePassword("password")
                skipItems(1)

                // WHEN
                viewModel.onLoginClicked()

                // THEN
                var updatedState = awaitItem()
                assertEquals(LoginState.Running, updatedState.loginState)

                updatedState = awaitItem()
                assertTrue(updatedState.afterLoginActions.isNotEmpty())

                val finalState = awaitItem()
                assertEquals(LoginState.Enabled, finalState.loginState)
            }
        }

    @Test
    fun `GIVEN failed login WHEN login is clicked THEN error is shown`() =
        runTest {
            // GIVEN
            val errorMessage = "Invalid credentials"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any(), any()),
            ) doReturn LoginResult.Error(errorMessage)

            initViewModel()

            viewModel.credentialsScreenState.test(timeout = 5.seconds) {
                skipItems(2)
                viewModel.updateUsername("user")
                skipItems(1)
                viewModel.updatePassword("password")
                skipItems(1)

                // WHEN
                viewModel.onLoginClicked()

                // THEN
                var updatedState = awaitItem()
                assertEquals(LoginState.Running, updatedState.loginState)

                updatedState = awaitItem()
                assertEquals(errorMessage, updatedState.errorMessage)

                updatedState = awaitItem()
                assertEquals(LoginState.Enabled, updatedState.loginState)
            }
        }

    @Test
    fun `GIVEN method call WHEN manage accounts is clicked THEN navigates to accounts`() =
        runTest {
            // GIVEN
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn true
            whenever(getIsSessionLockedUseCase()) doReturn false

            initViewModel()

            // WHEN
            viewModel.onManageAccountsClicked()

            // THEN
            verify(navigator).navigate(eq(LoginScreenState.Accounts), any())
        }

    private fun initViewModel(
        serverName: String? = "Test Server",
        serverUrl: String = "https://test.server.org",
        username: String? = null,
        allowRecovery: Boolean = true,
    ) {
        viewModel =
            CredentialsViewModel(
                navigator,
                getAvailableUsernames,
                getBiometricInfo,
                getHasOtherAccounts,
                loginUser,
                biometricLogin,
                openIdLogin,
                updateTrackingPermission,
                updateBiometricPermission,
                networkStatusProvider,
                serverName,
                serverUrl,
                username,
                allowRecovery,
                getIsSessionLockedUseCase,
                forgotPinUseCase,
                oidcInfo = null,
            )
    }
}
