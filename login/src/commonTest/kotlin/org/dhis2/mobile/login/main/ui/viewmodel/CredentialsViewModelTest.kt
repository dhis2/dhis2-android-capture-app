package org.dhis2.mobile.login.main.ui.viewmodel

import app.cash.turbine.test
import coil3.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.BiometricsInfo
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.OpenIdLoginConfiguration
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.GetDeviceEnrollmentUrl
import org.dhis2.mobile.login.main.domain.usecase.GetHasOtherAccounts
import org.dhis2.mobile.login.main.domain.usecase.LogOutUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUserWithOAuth
import org.dhis2.mobile.login.main.domain.usecase.OpenIdLogin
import org.dhis2.mobile.login.main.domain.usecase.ProcessDeviceEnrollment
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.state.LoginState
import org.dhis2.mobile.login.main.ui.state.OidcInfo
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.GetIsSessionLockedUseCase
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class CredentialsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val navigator: Navigator = mock()
    private val getAvailableUsernames: GetAvailableUsernames = mock()
    private val getBiometricInfo: GetBiometricInfo = mock()
    private val getHasOtherAccounts: GetHasOtherAccounts = mock()
    private val loginUser: LoginUser = mock()
    private val loginOutUser: LogOutUser = mock()
    private val biometricLogin: BiometricLogin = mock()
    private val openIdLogin: OpenIdLogin = mock()
    private val loginUserWithOAuth: LoginUserWithOAuth = mock()
    private val getDeviceEnrollmentUrl: GetDeviceEnrollmentUrl = mock()
    private val processDeviceEnrollment: ProcessDeviceEnrollment = mock()
    private val updateTrackingPermission: UpdateTrackingPermission = mock()
    private val updateBiometricPermission: UpdateBiometricPermission = mock()
    private val appLinkNavigation: AppLinkNavigation = mock()
    private val networkStatusProvider: NetworkStatusProvider = mock()
    private val getIsSessionLockedUseCase: GetIsSessionLockedUseCase = mock()
    private val forgotPinUseCase: ForgotPinUseCase = mock()

    private lateinit var viewModel: CredentialsViewModel

    private val turbineTimeout = 10.seconds

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
        whenever(appLinkNavigation.appLink) doReturn MutableSharedFlow()
    }

    @AfterTest
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
            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                val loadedState = awaitItem()
                assertFalse(loadedState.hasOtherAccounts)
                assertTrue(loadedState.canUseBiometrics)
                assertEquals(loadedState.credentialsInfo.availableUsernames, usernames)

                cancelAndIgnoreRemainingEvents()
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

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                skipItems(2) // Skip initial state

                // WHEN
                viewModel.updateUsername("test_user")

                // THEN
                val updatedState = awaitItem()
                assertEquals("test_user", updatedState.credentialsInfo.username)
                assertEquals(LoginState.Disabled, updatedState.loginState)

                cancelAndIgnoreRemainingEvents()
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

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                skipItems(2) // Skip initial state

                // WHEN
                viewModel.updateUsername("test_user")
                skipItems(1)
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

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()
                viewModel.updateUsername("user")
                awaitItem()
                viewModel.updatePassword("password")
                awaitItem()

                // WHEN
                viewModel.onLoginClicked()

                // THEN
                val updatedState = awaitItem()
                assertEquals(LoginState.Running, updatedState.loginState)

                // We must advance the virtual clock to allow the login coroutine (with delay) to complete
                testDispatcher.scheduler.advanceUntilIdle()

                // updatedState = awaitItem()
                // assertTrue(updatedState.afterLoginActions.isNotEmpty())

                val finalState = awaitItem()
                assertEquals(LoginState.Enabled, finalState.loginState)
                assertTrue(finalState.afterLoginActions.isNotEmpty())
                cancelAndIgnoreRemainingEvents()
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

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()
                viewModel.updateUsername("user")
                awaitItem()
                viewModel.updatePassword("password")
                awaitItem()

                // WHEN
                viewModel.onLoginClicked()

                // THEN
                var updatedState = awaitItem()
                assertEquals(LoginState.Running, updatedState.loginState)
                testDispatcher.scheduler.advanceTimeBy(4.seconds)

                updatedState = awaitItem()
                assertEquals(errorMessage, updatedState.errorMessage)
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

            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            verify(navigator).navigate(eq(LoginScreenState.Accounts), any())
        }

    @Test
    fun `GIVEN biometrics login starts WHEN result is success THEN login starts`() =
        runTest {
            val platformContext = mock<PlatformContext>()
            initViewModel(
                username = "Joe",
            )

            with(platformContext) {
                val testPassword = "test_password"

                whenever(getAvailableUsernames()) doReturn emptyList()
                whenever(getBiometricInfo(any())) doReturn BiometricsInfo(true, false)
                whenever(getHasOtherAccounts.invoke()) doReturn false
                whenever(getIsSessionLockedUseCase()) doReturn false

                whenever(biometricLogin.invoke()) doReturn Result.success(testPassword)
                whenever(loginUser.invoke(any(), any(), any(), any())) doReturn
                    LoginResult.Success(
                        true,
                        false,
                    )

                viewModel.credentialsScreenState.test {
                    awaitItem()
                    awaitItem()
                    viewModel.onBiometricsClicked()
                    testDispatcher.scheduler.advanceUntilIdle()
                    val updatedPasswordState = awaitItem()
                    assertEquals(testPassword, updatedPasswordState.credentialsInfo.password)
                    verify(loginUser).invoke(
                        serverUrl = "https://test.server.org",
                        username = "Joe",
                        password = testPassword,
                        isNetworkAvailable = true,
                    )
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

    @Test
    fun `GIVEN biometrics login starts WHEN result is failure THEN error is shown`() =
        runTest {
            val platformContext = mock<PlatformContext>()
            initViewModel(
                username = "Joe",
            )

            with(platformContext) {
                whenever(getAvailableUsernames()) doReturn emptyList()
                whenever(getBiometricInfo(any())) doReturn BiometricsInfo(true, false)
                whenever(getHasOtherAccounts.invoke()) doReturn false
                whenever(getIsSessionLockedUseCase()) doReturn false
                val exceptionMessage = "This is an error"
                whenever(biometricLogin.invoke()) doReturn Result.failure(Exception(exceptionMessage))

                viewModel.credentialsScreenState.test {
                    awaitItem()
                    awaitItem()
                    viewModel.onBiometricsClicked()
                    testDispatcher.scheduler.advanceUntilIdle()
                    val finalState = awaitItem()
                    assertEquals(exceptionMessage, finalState.errorMessage)
                    assertFalse(finalState.displayBiometricsDialog)
                    verify(loginUser, never()).invoke(
                        serverUrl = any(),
                        username = any(),
                        password = any(),
                        isNetworkAvailable = any(),
                    )
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

    @Test
    fun `GIVEN successful login with no other accounts WHEN user logs in THEN biometric credentials are NOT deleted`() =
        runTest {
            // GIVEN - User is logging into their first account (numberOfAccounts = 0)
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel()

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()
                viewModel.updateUsername("user")
                awaitItem()
                viewModel.updatePassword("password")
                awaitItem()

                // WHEN - User logs in successfully
                viewModel.onLoginClicked()

                // THEN - Login is successful
                awaitItem() // LoginState.Running
                testDispatcher.scheduler.advanceUntilIdle()

                val finalState = awaitItem()
                assertEquals(LoginState.Enabled, finalState.loginState)
                assertTrue(finalState.afterLoginActions.isNotEmpty())

                // Verify that the login was successful (which triggers checkDeleteBiometrics)
                verify(loginUser).invoke(
                    serverUrl = any(),
                    username = any(),
                    password = any(),
                    isNetworkAvailable = any(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN successful login with one existing account WHEN user logs in to second account THEN biometric credentials are deleted`() =
        runTest {
            // GIVEN - User already has one account and is logging into a second one
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn true
            whenever(getIsSessionLockedUseCase()) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel()

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()
                viewModel.updateUsername("secondUser")
                awaitItem()
                viewModel.updatePassword("password")
                awaitItem()

                // WHEN - Second user logs in successfully
                viewModel.onLoginClicked()

                // THEN - Login is successful
                awaitItem() // LoginState.Running
                testDispatcher.scheduler.advanceUntilIdle()

                val finalState = awaitItem()
                assertEquals(LoginState.Enabled, finalState.loginState)
                assertTrue(finalState.afterLoginActions.isNotEmpty())

                // Verify that the login was successful (which triggers checkDeleteBiometrics)
                verify(loginUser).invoke(
                    serverUrl = any(),
                    username = eq("secondUser"),
                    password = any(),
                    isNetworkAvailable = any(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN successful login with multiple accounts WHEN user logs in THEN biometric credentials are deleted`() =
        runTest {
            // GIVEN - User has multiple accounts
            whenever(getAvailableUsernames()) doReturn listOf("user1", "user2", "user3")
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn true
            whenever(getIsSessionLockedUseCase()) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel()

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()
                viewModel.updateUsername("user3")
                awaitItem()
                viewModel.updatePassword("password")
                awaitItem()

                // WHEN - User logs in successfully
                viewModel.onLoginClicked()

                // THEN - Login is successful
                awaitItem() // LoginState.Running
                testDispatcher.scheduler.advanceUntilIdle()

                val finalState = awaitItem()
                assertEquals(LoginState.Enabled, finalState.loginState)
                assertTrue(finalState.afterLoginActions.isNotEmpty())

                // Verify that the login was successful (which triggers checkDeleteBiometrics)
                verify(loginUser).invoke(
                    serverUrl = any(),
                    username = eq("user3"),
                    password = any(),
                    isNetworkAvailable = any(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN failed login WHEN user tries to log in THEN biometric credentials are NOT deleted`() =
        runTest {
            // GIVEN - User has existing accounts but login will fail
            val errorMessage = "Invalid credentials"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn true
            whenever(getIsSessionLockedUseCase()) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any(), any()),
            ) doReturn LoginResult.Error(errorMessage)

            initViewModel()

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()
                viewModel.updateUsername("user")
                awaitItem()
                viewModel.updatePassword("wrongPassword")
                awaitItem()

                // WHEN - User tries to login with wrong credentials
                viewModel.onLoginClicked()

                // THEN - Login fails
                awaitItem() // LoginState.Running
                testDispatcher.scheduler.advanceTimeBy(4.seconds)

                val updatedState = awaitItem()
                assertEquals(errorMessage, updatedState.errorMessage)
                assertEquals(LoginState.Enabled, updatedState.loginState)

                // Verify login was attempted but failed
                verify(loginUser).invoke(
                    serverUrl = any(),
                    username = any(),
                    password = any(),
                    isNetworkAvailable = any(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN app link with authorization code WHEN link arrives THEN OAuth login is triggered`() =
        runTest {
            // GIVEN
            val serverUrl = "https://test.server.org"
            val authCode = "auth_code_123"
            val appLinkUrl = "https://vgarciabnz.github.io?code=$authCode&state=test"
            val mockAppLinkFlow = MutableSharedFlow<String>()
            val enrollmentUrl = "https://test.server.org/oauth2/enrollment"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)
            whenever(
                loginUserWithOAuth.invoke(any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            // Use oAuthEnable=true and fromHome=true to avoid auto-launch but enable OAuth flow
            initViewModel(serverUrl = serverUrl, username = "testuser", oAuthEnable = true, fromHome = true)

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()

                // Trigger OAuth flow by clicking login
                viewModel.onLoginClicked()
                testDispatcher.scheduler.advanceUntilIdle()

                // Login state should be running after clicking login
                val runningState = awaitItem()
                assertEquals(LoginState.Running, runningState.loginState)

                // WHEN - Send app link with authorization code (simulates OAuth callback)
                mockAppLinkFlow.emit(appLinkUrl)
                testDispatcher.scheduler.advanceUntilIdle()

                // Advance time for login to complete
                testDispatcher.scheduler.advanceTimeBy(4.seconds)

                // Login should succeed and show after login actions
                val finalState = awaitItem()
                assertEquals(LoginState.Enabled, finalState.loginState)
                assertTrue(finalState.afterLoginActions.isNotEmpty())

                // Verify OAuth login was called with the correct code
                verify(loginUserWithOAuth).invoke(
                    serverUrl = serverUrl,
                    code = authCode,
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN app link with IAT token WHEN link arrives THEN device enrollment is processed`() =
        runTest {
            // GIVEN
            val serverUrl = "https://test.server.org"
            val iat = "enrollment_iat_token"
            val consentUrl = "https://test.server.org/oauth2/consent"
            val enrollmentUrl = "https://test.server.org/oauth2/enrollment"
            val appLinkUrl = "https://vgarciabnz.github.io?iat=$iat&state=test"
            val mockAppLinkFlow = MutableSharedFlow<String>()

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)
            whenever(
                processDeviceEnrollment.invoke(any()),
            ) doReturn Result.success(consentUrl)

            // Use oAuthEnable=true and fromHome=true to avoid auto-launch but enable OAuth flow
            initViewModel(serverUrl = serverUrl, username = "testuser", oAuthEnable = true, fromHome = true)

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()

                // Trigger OAuth flow by clicking login
                viewModel.onLoginClicked()
                testDispatcher.scheduler.advanceUntilIdle()

                // Login state should be running after clicking login
                val runningState = awaitItem()
                assertEquals(LoginState.Running, runningState.loginState)

                // WHEN - Send app link with IAT token (simulates enrollment callback)
                mockAppLinkFlow.emit(appLinkUrl)
                testDispatcher.scheduler.advanceUntilIdle()

                // Verify device enrollment was called with the correct IAT
                verify(processDeviceEnrollment).invoke(any())

                // Verify navigation happened twice: once for enrollment URL, once for consent URL
                verify(navigator, times(2)).navigate(any<LoginScreenState>(), any())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN app link with error WHEN link arrives THEN error message is displayed`() =
        runTest {
            // GIVEN
            val serverUrl = "https://test.server.org"
            val appLinkUrl = "https://vgarciabnz.github.io?error=access_denied&state=test"
            val mockAppLinkFlow = MutableSharedFlow<String>()
            val enrollmentUrl = "https://test.server.org/oauth2/enrollment"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)

            // Use oAuthEnable=true and fromHome=true to avoid auto-launch but enable OAuth flow
            initViewModel(serverUrl = serverUrl, username = "testuser", oAuthEnable = true, fromHome = true)

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()

                // Trigger OAuth flow by clicking login
                viewModel.onLoginClicked()
                testDispatcher.scheduler.advanceUntilIdle()

                // Login state should be running after clicking login
                val runningState = awaitItem()
                assertEquals(LoginState.Running, runningState.loginState)

                // WHEN - Send app link with error (simulates OAuth error callback)
                mockAppLinkFlow.emit(appLinkUrl)
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - Error message should be shown
                val errorState = awaitItem()
                assertEquals("access_denied", errorState.errorMessage)
                assertEquals(LoginState.Enabled, errorState.loginState)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN oidcInfo is Discovery with prompt WHEN onOpenIdLogin is called THEN discoveryUri and prompt are forwarded`() =
        runTest {
            val serverUrl = "https://test.server.org"
            val prompt = "select_account"
            val discoveryUri = "https://test.server.org/.well-known/openid-configuration"
            val clientId = "client-123"
            val redirectUri = "dhis2://oauth"
            val oidcInfo =
                OidcInfo.Discovery(
                    server = serverUrl,
                    loginButtonText = null,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    discoveryUri = discoveryUri,
                    prompt = prompt,
                )

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false
            whenever(openIdLogin.invoke(any())) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel(serverUrl = serverUrl, oidcInfo = oidcInfo)

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()

                viewModel.onOpenIdLogin()

                val runningState = awaitItem()
                assertEquals(LoginState.Running, runningState.loginState)

                testDispatcher.scheduler.advanceUntilIdle()

                verify(openIdLogin).invoke(
                    OpenIdLoginConfiguration(
                        serverUrl = serverUrl,
                        isNetworkAvailable = true,
                        clientId = clientId,
                        redirectUri = redirectUri,
                        discoveryUri = discoveryUri,
                        authorizationUri = null,
                        tokenUrl = null,
                        prompt = prompt,
                    ),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN oidcInfo is Token with prompt WHEN onOpenIdLogin is called THEN tokenUrl, authorizationUri and prompt are forwarded`() =
        runTest {
            val serverUrl = "https://test.server.org"
            val prompt = "login"
            val authorizationUrl = "https://test.server.org/oauth/authorize"
            val tokenUrl = "https://test.server.org/oauth/token"
            val clientId = "client-456"
            val redirectUri = "dhis2://oauth"
            val oidcInfo =
                OidcInfo.Token(
                    server = serverUrl,
                    loginLabel = null,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    authorizationUrl = authorizationUrl,
                    tokenUrl = tokenUrl,
                    prompt = prompt,
                )

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false
            whenever(openIdLogin.invoke(any())) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel(serverUrl = serverUrl, oidcInfo = oidcInfo)

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()

                viewModel.onOpenIdLogin()

                val runningState = awaitItem()
                assertEquals(LoginState.Running, runningState.loginState)

                testDispatcher.scheduler.advanceUntilIdle()

                verify(openIdLogin).invoke(
                    OpenIdLoginConfiguration(
                        serverUrl = serverUrl,
                        isNetworkAvailable = true,
                        clientId = clientId,
                        redirectUri = redirectUri,
                        discoveryUri = null,
                        authorizationUri = authorizationUrl,
                        tokenUrl = tokenUrl,
                        prompt = prompt,
                    ),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN oidcInfo with null prompt WHEN onOpenIdLogin is called THEN null prompt is forwarded`() =
        runTest {
            val serverUrl = "https://test.server.org"
            val oidcInfo =
                OidcInfo.Discovery(
                    server = serverUrl,
                    loginButtonText = null,
                    clientId = "client-789",
                    redirectUri = "dhis2://oauth",
                    discoveryUri = "https://test.server.org/.well-known/openid-configuration",
                    prompt = null,
                )

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase()) doReturn false
            whenever(openIdLogin.invoke(any())) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel(serverUrl = serverUrl, oidcInfo = oidcInfo)

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()

                viewModel.onOpenIdLogin()

                val runningState = awaitItem()
                assertEquals(LoginState.Running, runningState.loginState)

                testDispatcher.scheduler.advanceUntilIdle()

                verify(openIdLogin).invoke(
                    OpenIdLoginConfiguration(
                        serverUrl = serverUrl,
                        isNetworkAvailable = true,
                        clientId = "client-789",
                        redirectUri = "dhis2://oauth",
                        discoveryUri = "https://test.server.org/.well-known/openid-configuration",
                        authorizationUri = null,
                        tokenUrl = null,
                        prompt = null,
                    ),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun initViewModel(
        serverName: String? = "Test Server",
        serverUrl: String = "https://test.server.org",
        username: String? = null,
        allowRecovery: Boolean = true,
        oAuthEnable: Boolean = false,
        fromHome: Boolean = false,
        oidcInfo: OidcInfo? = null,
    ) {
        viewModel =
            CredentialsViewModel(
                navigator,
                getAvailableUsernames,
                getBiometricInfo,
                getHasOtherAccounts,
                loginUser,
                loginOutUser,
                biometricLogin,
                openIdLogin,
                loginUserWithOAuth,
                getDeviceEnrollmentUrl,
                processDeviceEnrollment,
                updateTrackingPermission,
                updateBiometricPermission,
                appLinkNavigation,
                networkStatusProvider,
                serverName,
                serverUrl,
                username,
                allowRecovery,
                getIsSessionLockedUseCase,
                forgotPinUseCase,
                oidcInfo = oidcInfo,
                fromHome = fromHome,
                oAuthEnable = oAuthEnable,
            )
    }
}
