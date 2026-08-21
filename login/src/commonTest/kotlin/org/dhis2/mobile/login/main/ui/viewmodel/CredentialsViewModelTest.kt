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
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.BiometricsInfo
import org.dhis2.mobile.login.main.domain.model.CredentialsEntryMode
import org.dhis2.mobile.login.main.domain.model.DeviceEnrollmentInfo
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.OpenIdLoginConfiguration
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.GetDeviceEnrollmentUrl
import org.dhis2.mobile.login.main.domain.usecase.GetHasOtherAccounts
import org.dhis2.mobile.login.main.domain.usecase.GetOAuthLogoutUrl
import org.dhis2.mobile.login.main.domain.usecase.GetSessionRenewalUrl
import org.dhis2.mobile.login.main.domain.usecase.LogOutUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUserOffline
import org.dhis2.mobile.login.main.domain.usecase.LoginUserWithOAuth
import org.dhis2.mobile.login.main.domain.usecase.OpenIdLogin
import org.dhis2.mobile.login.main.domain.usecase.ProcessDeviceEnrollment
import org.dhis2.mobile.login.main.domain.usecase.SetOAuthPin
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.provider.CredentialsResourceProvider
import org.dhis2.mobile.login.main.ui.state.AfterLoginAction
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
import kotlin.test.assertIs
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
    private val getOAuthLogoutUrl: GetOAuthLogoutUrl = mock()
    private val getSessionRenewalUrl: GetSessionRenewalUrl = mock()
    private val processDeviceEnrollment: ProcessDeviceEnrollment = mock()
    private val updateTrackingPermission: UpdateTrackingPermission = mock()
    private val updateBiometricPermission: UpdateBiometricPermission = mock()
    private val appLinkNavigation: AppLinkNavigation = mock()
    private val networkStatusProvider: NetworkStatusProvider = mock()
    private val getIsSessionLockedUseCase: GetIsSessionLockedUseCase = mock()
    private val forgotPinUseCase: ForgotPinUseCase = mock()
    private val setOAuthPin: SetOAuthPin = mock()
    private val loginUserOfflineWithCode: LoginUserOffline = mock()
    private val credentialsResourceProvider: CredentialsResourceProvider = mock()

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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            // WHEN
            initViewModel(serverUrl = serverUrl)

            // THEN
            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                val loadedState = awaitItem()
                assertFalse(loadedState.hasOtherAccounts)
                assertTrue(loadedState.canUseBiometrics)
                assertEquals(loadedState.credentialsInfo?.availableUsernames, usernames)

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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            initViewModel()

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                skipItems(2) // Skip initial state

                // WHEN
                viewModel.updateUsername("test_user")

                // THEN
                val updatedState = awaitItem()
                assertEquals("test_user", updatedState.credentialsInfo?.username)
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            initViewModel()

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                skipItems(2) // Skip initial state

                // WHEN
                viewModel.updateUsername("test_user")
                skipItems(1)
                viewModel.updatePassword("test_password")

                // THEN
                val updatedState = awaitItem()
                assertEquals("test_password", updatedState.credentialsInfo?.password)
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any()),
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any()),
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

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
                whenever(getIsSessionLockedUseCase(any())) doReturn false

                whenever(biometricLogin.invoke()) doReturn Result.success(testPassword)
                whenever(loginUser.invoke(any(), any(), any())) doReturn
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
                    assertEquals(testPassword, updatedPasswordState.credentialsInfo?.password)
                    verify(loginUser).invoke(
                        serverUrl = "https://test.server.org",
                        username = "Joe",
                        password = testPassword,
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
                whenever(getIsSessionLockedUseCase(any())) doReturn false
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any()),
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any()),
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any()),
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            whenever(
                loginUser.invoke(any(), any(), any()),
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
            val appLinkUrl = "https://test.redirect.org?code=$authCode&state=test"
            val mockAppLinkFlow = MutableSharedFlow<String>()
            val enrollmentUrl = "https://test.server.org/oauth2/enrollment"
            val logoutUrl =
                "$serverUrl/dhis-web-commons-security/logout.action?redirect_uri=dhis2oauth://oauth"
            val state = "test"
            val logoutCallbackUrl = "https://test.redirect.org?state=$state"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)
            whenever(getOAuthLogoutUrl(any())) doReturn Result.success(logoutUrl)
            whenever(
                loginUserWithOAuth.invoke(any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel(
                serverUrl = serverUrl,
                username = "testuser",
                entryMode = CredentialsEntryMode.NEW_ACCOUNT_OAUTH,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                // The enrollment flow starts and the view model listens for OAuth callbacks
                testDispatcher.scheduler.advanceUntilIdle()
                verify(navigator).navigate(
                    eq(LoginScreenState.OauthAuthentication(selectedServer = enrollmentUrl)),
                    any(),
                )

                // WHEN - Send app link with authorization code (simulates OAuth callback)
                mockAppLinkFlow.emit(appLinkUrl)
                testDispatcher.scheduler.advanceUntilIdle()

                // Advance time for login to complete
                testDispatcher.scheduler.advanceTimeBy(4.seconds)
                testDispatcher.scheduler.advanceUntilIdle()

                // Verify OAuth login was called with the correct code
                verify(loginUserWithOAuth).invoke(
                    serverUrl = serverUrl,
                    code = authCode,
                    state = state,
                )

                // THEN - the server session is cleared before entering the app:
                // navigate to the logout URL and defer the after-login actions
                verify(navigator).navigate(
                    eq(LoginScreenState.OauthAuthentication(selectedServer = logoutUrl)),
                    any(),
                )
                assertTrue(expectMostRecentItem().afterLoginActions.isEmpty())

                // WHEN - the logout redirect returns (no code/iat/error)
                mockAppLinkFlow.emit(logoutCallbackUrl)
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - creating the mandatory offline credential is the first post-login
                // action, gating navigation into the app
                val awaitingState = expectMostRecentItem()
                assertEquals(LoginState.Enabled, awaitingState.loginState)
                assertIs<AfterLoginAction.CreateOfflineCredential>(
                    awaitingState.afterLoginActions.firstOrNull(),
                )

                // WHEN - the user creates the mandatory offline credential
                whenever(setOAuthPin("1234")) doReturn Result.success(Unit)
                viewModel.onOfflineCredentialCreated("1234")
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - it is stored with the SDK and the create gate clears, leaving the
                // remaining post-login actions to run
                verify(setOAuthPin).invoke("1234")
                val finalState = expectMostRecentItem()
                assertTrue(finalState.afterLoginActions.none { it is AfterLoginAction.CreateOfflineCredential })
                assertTrue(finalState.afterLoginActions.isNotEmpty())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN the device is not registered WHEN the authorization code arrives THEN the OAuth login is rejected`() =
        runTest {
            // GIVEN - the SDK rejects the login response with OAUTH2_DEVICE_NOT_REGISTERED,
            // which reaches the view model as its mapped error message
            val serverUrl = "https://test.server.org"
            val authCode = "auth_code_123"
            val state = "test"
            val appLinkUrl = "https://test.redirect.org?code=$authCode&state=$state"
            val mockAppLinkFlow = MutableSharedFlow<String>()
            val enrollmentUrl = "https://test.server.org/oauth2/enrollment"
            val deviceNotRegisteredMessage = "Device not registered"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)
            whenever(
                loginUserWithOAuth.invoke(any(), any(), any()),
            ) doReturn LoginResult.Error(deviceNotRegisteredMessage)

            initViewModel(
                serverUrl = serverUrl,
                username = "testuser",
                entryMode = CredentialsEntryMode.NEW_ACCOUNT_OAUTH,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                // The enrollment flow starts and the view model listens for OAuth callbacks
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN - the authorization code arrives
                mockAppLinkFlow.emit(appLinkUrl)
                testDispatcher.scheduler.advanceUntilIdle()
                testDispatcher.scheduler.advanceTimeBy(4.seconds)
                testDispatcher.scheduler.advanceUntilIdle()

                verify(loginUserWithOAuth).invoke(
                    serverUrl = serverUrl,
                    code = authCode,
                    state = state,
                )

                // THEN - the device-not-registered message is shown and the user does not enter
                // the app: no logout hop and no post-login actions
                val errorState = expectMostRecentItem()
                assertEquals(deviceNotRegisteredMessage, errorState.errorMessage)
                assertEquals(LoginState.Enabled, errorState.loginState)
                assertTrue(errorState.afterLoginActions.isEmpty())
                verify(getOAuthLogoutUrl, never()).invoke(any())

                // AND - the OAuth flow is over, so later app links are ignored
                mockAppLinkFlow.emit("https://test.redirect.org?code=late_code&state=$state")
                testDispatcher.scheduler.advanceUntilIdle()
                verify(loginUserWithOAuth, never()).invoke(any(), eq("late_code"), any())

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
            val appLinkUrl = "https://test.redirect.org?iat=$iat&state=test"
            val mockAppLinkFlow = MutableSharedFlow<String>()

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)
            whenever(
                processDeviceEnrollment.invoke(any()),
            ) doReturn Result.success(consentUrl)

            initViewModel(
                serverUrl = serverUrl,
                username = "testuser",
                entryMode = CredentialsEntryMode.NEW_ACCOUNT_OAUTH,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                // The enrollment flow starts and the view model listens for OAuth callbacks
                testDispatcher.scheduler.advanceUntilIdle()
                verify(navigator).navigate(
                    eq(LoginScreenState.OauthAuthentication(selectedServer = enrollmentUrl)),
                    any(),
                )

                // WHEN - Send app link with IAT token (simulates enrollment callback)
                mockAppLinkFlow.emit(appLinkUrl)
                testDispatcher.scheduler.advanceUntilIdle()

                // Verify device enrollment was called with the correct IAT
                verify(processDeviceEnrollment).invoke(any())

                // Verify navigation to the consent URL
                verify(navigator).navigate(
                    eq(LoginScreenState.OauthAuthentication(selectedServer = consentUrl)),
                    any(),
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN the device registration is incomplete WHEN the enrollment token arrives THEN the OAuth flow is aborted`() =
        runTest {
            // GIVEN - the SDK rejects the enrollment response with OAUTH2_INCOMPLETE_REGISTRATION,
            // which reaches the view model as its mapped authentication error
            val serverUrl = "https://test.server.org"
            val iat = "enrollment_iat_token"
            val state = "test"
            val appLinkUrl = "https://test.redirect.org?iat=$iat&state=$state"
            val mockAppLinkFlow = MutableSharedFlow<String>()
            val enrollmentUrl = "https://test.server.org/oauth2/enrollment"
            val oauth2ErrorMessage = "There was an error when authenticating with OAuth2"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)
            whenever(
                processDeviceEnrollment.invoke(any()),
            ) doReturn Result.failure(DomainError.AuthenticationError(oauth2ErrorMessage))

            initViewModel(
                serverUrl = serverUrl,
                username = "testuser",
                entryMode = CredentialsEntryMode.NEW_ACCOUNT_OAUTH,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                // The enrollment flow starts and the view model listens for OAuth callbacks
                testDispatcher.scheduler.advanceUntilIdle()
                verify(navigator).navigate(
                    eq(LoginScreenState.OauthAuthentication(selectedServer = enrollmentUrl)),
                    any(),
                )

                // WHEN - the enrollment token arrives
                mockAppLinkFlow.emit(appLinkUrl)
                testDispatcher.scheduler.advanceUntilIdle()

                verify(processDeviceEnrollment).invoke(
                    DeviceEnrollmentInfo(
                        iat = iat,
                        serverURL = serverUrl,
                        state = state,
                    ),
                )

                // THEN - the OAuth2 error is shown and the consent step is never reached:
                // the enrollment navigation remains the only one
                val errorState = expectMostRecentItem()
                assertEquals(oauth2ErrorMessage, errorState.errorMessage)
                assertEquals(LoginState.Enabled, errorState.loginState)
                assertTrue(errorState.afterLoginActions.isEmpty())
                verify(navigator, times(1)).navigate(any(), any())

                // AND - the OAuth flow is over, so later app links are ignored
                mockAppLinkFlow.emit("https://test.redirect.org?code=late_code&state=$state")
                testDispatcher.scheduler.advanceUntilIdle()
                verify(loginUserWithOAuth, never()).invoke(any(), any(), any())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN app link with error WHEN link arrives THEN error message is displayed`() =
        runTest {
            // GIVEN
            val serverUrl = "https://test.server.org"
            val state = "test"
            val appLinkUrl = "https://test.redirect.org?error=access_denied&state=$state"
            val mockAppLinkFlow = MutableSharedFlow<String>()
            val enrollmentUrl = "https://test.server.org/oauth2/enrollment"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)

            initViewModel(
                serverUrl = serverUrl,
                username = "testuser",
                entryMode = CredentialsEntryMode.NEW_ACCOUNT_OAUTH,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                // The enrollment flow starts and the view model listens for OAuth callbacks
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN - Send app link with error (simulates OAuth error callback)
                mockAppLinkFlow.emit(appLinkUrl)
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - Error message should be shown
                val errorState = expectMostRecentItem()
                assertEquals("access_denied", errorState.errorMessage)
                assertEquals(LoginState.Enabled, errorState.loginState)

                // AND - the OAuth flow is over, so later app links are ignored
                mockAppLinkFlow.emit("https://test.redirect.org?code=late_code&state=$state")
                testDispatcher.scheduler.advanceUntilIdle()
                verify(loginUserWithOAuth, never()).invoke(any(), any(), any())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN two view models sharing app links WHEN second OAuth account logs in THEN only the flow owner handles callbacks`() =
        runTest {
            // GIVEN - an existing account screen (stale) and a new OAuth account screen (owner)
            val staleServerUrl = "https://first.server.org"
            val oauthServerUrl = "https://second.server.org"
            val authCode = "auth_code_456"
            val state = "test"
            val enrollmentUrl = "$oauthServerUrl/oauth2/enrollment"
            val logoutUrl =
                "$oauthServerUrl/dhis-web-commons-security/logout.action?redirect_uri=dhis2oauth://oauth"
            val sharedAppLinkNavigation = AppLinkNavigation()

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn true
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)
            whenever(getOAuthLogoutUrl(any())) doReturn Result.success(logoutUrl)
            whenever(
                loginUserWithOAuth.invoke(any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            val staleViewModel =
                initViewModel(
                    serverUrl = staleServerUrl,
                    username = "firstUser",
                    entryMode = CredentialsEntryMode.EXISTING_OAUTH,
                    appLinkNavigation = sharedAppLinkNavigation,
                )
            val oauthViewModel =
                initViewModel(
                    serverUrl = oauthServerUrl,
                    entryMode = CredentialsEntryMode.NEW_ACCOUNT_OAUTH,
                    appLinkNavigation = sharedAppLinkNavigation,
                )

            oauthViewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                // Both view models load; only the new OAuth one starts its enrollment flow
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN - the authorization code and logout callbacks arrive
                sharedAppLinkNavigation.emit("https://test.redirect.org?code=$authCode&state=$state")
                testDispatcher.scheduler.advanceTimeBy(4.seconds)
                testDispatcher.scheduler.advanceUntilIdle()
                sharedAppLinkNavigation.emit("https://test.redirect.org?state=$state")
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - the OAuth flow owner completes the login and reaches the mandatory
                // offline-PIN creation step
                verify(loginUserWithOAuth).invoke(
                    serverUrl = oauthServerUrl,
                    code = authCode,
                    state = state,
                )
                val finalState = expectMostRecentItem()
                assertEquals(LoginState.Enabled, finalState.loginState)
                assertIs<AfterLoginAction.CreateOfflineCredential>(
                    finalState.afterLoginActions.firstOrNull(),
                )

                cancelAndIgnoreRemainingEvents()
            }

            // AND - the stale view model never consumed the callbacks
            verify(loginUserWithOAuth, never()).invoke(eq(staleServerUrl), any(), any())
            assertTrue(
                staleViewModel.credentialsScreenState.value.afterLoginActions
                    .isEmpty(),
            )
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
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(openIdLogin.invoke(any())) doReturn
                LoginResult.Success(
                    initialSyncDone = true,
                    displayTrackingMessage = false,
                )

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
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(openIdLogin.invoke(any())) doReturn
                LoginResult.Success(
                    initialSyncDone = true,
                    displayTrackingMessage = false,
                )

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
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(openIdLogin.invoke(any())) doReturn
                LoginResult.Success(
                    initialSyncDone = true,
                    displayTrackingMessage = false,
                )

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

    @Test
    fun `GIVEN EXISTING_OAUTH WHEN the offline credential is entered THEN offline login runs with it as password`() =
        runTest {
            // GIVEN
            val serverUrl = "https://test.server.org"
            val username = "testUser"
            val pin = "1234"
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn
                BiometricsInfo(
                    canUseBiometrics = false,
                    displayBiometricsMessageAfterLogin = false,
                )
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(true)) doReturn true
            whenever(loginUserOfflineWithCode.invoke(serverUrl, username, pin)) doReturn
                LoginResult.Success(displayTrackingMessage = false, initialSyncDone = true)

            initViewModel(
                serverUrl = serverUrl,
                username = username,
                entryMode = CredentialsEntryMode.EXISTING_OAUTH,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                val lockedState = awaitItem()
                assertTrue(lockedState.isSessionLocked)

                // WHEN - the user enters their offline credential
                viewModel.onOfflineCredentialEntered(pin)

                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - the entered credential is used as the offline login code (verbatim)
                verify(loginUserOfflineWithCode).invoke(serverUrl, username, pin)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN too many failed offline attempts WHEN lockout is triggered THEN login is disabled then re-enabled`() =
        runTest {
            // GIVEN
            val serverUrl = "https://test.server.org"
            val username = "testUser"
            val pin = "1234"
            val lockoutSeconds = 3
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn
                BiometricsInfo(
                    canUseBiometrics = false,
                    displayBiometricsMessageAfterLogin = false,
                )
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(true)) doReturn true
            val countdownMessage = "Locked out, try again shortly"
            whenever(loginUserOfflineWithCode.invoke(serverUrl, username, pin)) doReturn
                LoginResult.LockOut(lockoutSeconds)
            whenever(credentialsResourceProvider.getLockoutCountdownMessage(any())) doReturn countdownMessage

            initViewModel(
                serverUrl = serverUrl,
                username = username,
                entryMode = CredentialsEntryMode.EXISTING_OAUTH,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                val lockedState = awaitItem()
                assertTrue(lockedState.isSessionLocked)

                // WHEN - the offline credential triggers a lockout
                viewModel.onOfflineCredentialEntered(pin)

                // Advance just past the login spinner's minimum duration, landing right after
                // the countdown's first tick
                testDispatcher.scheduler.advanceTimeBy(3.1.seconds)

                // THEN - login is disabled and the countdown message is shown
                val lockedOutState = expectMostRecentItem()
                assertEquals(LoginState.Disabled, lockedOutState.loginState)
                assertEquals(countdownMessage, lockedOutState.errorMessage)

                // WHEN - the lockout duration fully elapses
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - login is re-enabled and the countdown message is cleared
                val finalState = expectMostRecentItem()
                assertEquals(LoginState.Enabled, finalState.loginState)
                assertEquals(null, finalState.errorMessage)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN EXISTING_OAUTH WHEN biometric login succeeds THEN it logs in via the offline-credential path`() =
        runTest {
            // GIVEN - biometrics decrypts the stored offline PIN
            val platformContext = mock<PlatformContext>()
            val serverUrl = "https://test.server.org"
            val username = "testUser"
            val pin = "1234"

            initViewModel(
                serverUrl = serverUrl,
                username = username,
                entryMode = CredentialsEntryMode.EXISTING_OAUTH,
            )

            with(platformContext) {
                whenever(getAvailableUsernames()) doReturn emptyList()
                whenever(getBiometricInfo(any())) doReturn BiometricsInfo(true, false)
                whenever(getHasOtherAccounts.invoke()) doReturn false
                whenever(getIsSessionLockedUseCase(any())) doReturn false
                whenever(biometricLogin.invoke()) doReturn Result.success(pin)
                whenever(loginUserOfflineWithCode.invoke(serverUrl, username, pin)) doReturn
                    LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

                viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                    awaitItem()
                    awaitItem()

                    // WHEN
                    viewModel.onBiometricsClicked()
                    testDispatcher.scheduler.advanceUntilIdle()

                    // THEN - the biometric credential is used directly as the offline-login
                    // password (via onOfflineCredentialEntered), not routed through
                    // updatePassword()/onLoginClicked() as before
                    verify(loginUserOfflineWithCode).invoke(serverUrl, username, pin)

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

    @Test
    fun `GIVEN NEW_ACCOUNT_OAUTH WHEN biometrics enabled after offline pin creation THEN the pin is saved as the biometric credential`() =
        runTest {
            // GIVEN - a fresh OAuth account that just created its mandatory offline PIN
            val platformContext = mock<PlatformContext>()
            val serverUrl = "https://test.server.org"
            val enrollmentUrl = "$serverUrl/oauth2/enrollment"
            val pin = "1234"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(getDeviceEnrollmentUrl(any())) doReturn Result.success(enrollmentUrl)
            whenever(setOAuthPin(pin)) doReturn Result.success(Unit)

            initViewModel(serverUrl = serverUrl, entryMode = CredentialsEntryMode.NEW_ACCOUNT_OAUTH)

            with(platformContext) {
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN - the mandatory offline PIN is created after OAuth login
                viewModel.onOfflineCredentialCreated(pin)
                testDispatcher.scheduler.advanceUntilIdle()

                // AND - the user later opts in to biometrics
                viewModel.onEnableBiometrics(true)
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - the stored offline PIN (not the empty basic-auth password) becomes
                // the biometric credential
                verify(updateBiometricPermission).invoke(serverUrl, "", pin, true)
            }
        }

    @Test
    fun `GIVEN a basic account WHEN biometrics enabled THEN the typed password is saved as the biometric credential`() =
        runTest {
            // GIVEN - a basic-auth account with username/password typed in
            val platformContext = mock<PlatformContext>()
            val serverUrl = "https://test.server.org"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(true, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false

            initViewModel(serverUrl = serverUrl)

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()
                viewModel.updateUsername("user")
                awaitItem()
                viewModel.updatePassword("myPassword")
                awaitItem()

                with(platformContext) {
                    // WHEN
                    viewModel.onEnableBiometrics(true)
                    testDispatcher.scheduler.advanceUntilIdle()

                    // THEN - the untouched branch still saves the typed password, not offlinePin
                    verify(updateBiometricPermission).invoke(serverUrl, "user", "myPassword", true)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN a username with whitespace WHEN login is clicked THEN it is trimmed before the login use case`() =
        runTest {
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(
                loginUser.invoke(any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel(serverUrl = "https://test.server.org", username = null)

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                awaitItem()
                viewModel.updateUsername("  user  ")
                awaitItem()
                viewModel.updatePassword("password")
                awaitItem()

                viewModel.onLoginClicked()
                awaitItem() // LoginState.Running
                testDispatcher.scheduler.advanceUntilIdle()

                // The typed username is trimmed at the boundary before reaching the use case
                verify(loginUser).invoke(
                    serverUrl = "https://test.server.org",
                    username = "user",
                    password = "password",
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN EXISTING_OAUTH initial landing WHEN loaded THEN offline dialog is not auto-shown until Login is tapped`() =
        runTest {
            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(true)) doReturn true

            initViewModel(
                entryMode = CredentialsEntryMode.EXISTING_OAUTH,
                autoPromptLogin = false,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                awaitItem()
                // Passive initial landing: the offline-credential dialog is NOT auto-presented
                val loadedState = awaitItem()
                assertFalse(loadedState.isSessionLocked)

                // WHEN - the user taps Login
                viewModel.onLoginClicked()

                // THEN - the offline-credential dialog is presented
                assertTrue(awaitItem().isSessionLocked)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN an existing OAuth account WHEN the session is renewed THEN the authorization url is opened`() =
        runTest {
            // GIVEN - an account whose tokens can no longer reach the server
            val serverUrl = "https://test.server.org"
            val authorizationUrl = "$serverUrl/oauth2/authorize"
            val authCode = "auth_code_123"
            val state = "test"
            val mockAppLinkFlow = MutableSharedFlow<String>()

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getSessionRenewalUrl(any())) doReturn Result.success(authorizationUrl)
            whenever(getOAuthLogoutUrl(any())) doReturn Result.success("$serverUrl/logout")
            whenever(
                loginUserWithOAuth.invoke(any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel(
                serverUrl = serverUrl,
                username = "testuser",
                entryMode = CredentialsEntryMode.EXISTING_OAUTH,
                autoPromptLogin = false,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN - the user asks to log in again to renew the session
                viewModel.onRenewSession()
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - the browser is opened on the url built for this account, and the
                // enrollment ceremony is not started again
                verify(navigator).navigate(
                    eq(LoginScreenState.OauthAuthentication(selectedServer = authorizationUrl)),
                    any(),
                )
                verify(getDeviceEnrollmentUrl, never()).invoke(any())

                // AND - the redirect coming back from the browser is handled as a login
                mockAppLinkFlow.emit("https://test.redirect.org?code=$authCode&state=$state")
                testDispatcher.scheduler.advanceUntilIdle()
                testDispatcher.scheduler.advanceTimeBy(4.seconds)
                testDispatcher.scheduler.advanceUntilIdle()

                verify(loginUserWithOAuth).invoke(
                    serverUrl = serverUrl,
                    code = authCode,
                    state = state,
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN the renewal url cannot be built WHEN the session is renewed THEN no browser is opened`() =
        runTest {
            // GIVEN - the server cannot be checked, so there is no url to send the user to
            val serverUrl = "https://test.server.org"
            val errorMessage = "You are offline. Connect to the internet and try again."

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(getSessionRenewalUrl(any())) doReturn
                Result.failure(DomainError.ServerError(errorMessage))

            initViewModel(
                serverUrl = serverUrl,
                username = "testuser",
                entryMode = CredentialsEntryMode.EXISTING_OAUTH,
                autoPromptLogin = false,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN
                viewModel.onRenewSession()
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - the reason is shown and the user stays on the login screen, still able
                // to open the account offline with the PIN
                val errorState = expectMostRecentItem()
                assertEquals(errorMessage, errorState.errorMessage)
                assertEquals(LoginState.Enabled, errorState.loginState)
                verify(navigator, never()).navigate(any(), any())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN a renewed session WHEN the login completes THEN the offline credential is created again`() =
        runTest {
            // GIVEN - an existing OAuth account renewing its session through the browser
            val serverUrl = "https://test.server.org"
            val authorizationUrl = "$serverUrl/oauth2/authorize"
            val logoutUrl = "$serverUrl/logout"
            val state = "test"
            val mockAppLinkFlow = MutableSharedFlow<String>()

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(any())) doReturn false
            whenever(appLinkNavigation.appLink) doReturn mockAppLinkFlow
            whenever(getSessionRenewalUrl(any())) doReturn Result.success(authorizationUrl)
            whenever(getOAuthLogoutUrl(any())) doReturn Result.success(logoutUrl)
            whenever(
                loginUserWithOAuth.invoke(any(), any(), any()),
            ) doReturn LoginResult.Success(initialSyncDone = true, displayTrackingMessage = false)

            initViewModel(
                serverUrl = serverUrl,
                username = "testuser",
                entryMode = CredentialsEntryMode.EXISTING_OAUTH,
                autoPromptLogin = false,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN - the renewal completes: code redirect, then the logout hop
                viewModel.onRenewSession()
                testDispatcher.scheduler.advanceUntilIdle()
                mockAppLinkFlow.emit("https://test.redirect.org?code=auth_code_123&state=$state")
                testDispatcher.scheduler.advanceUntilIdle()
                testDispatcher.scheduler.advanceTimeBy(4.seconds)
                testDispatcher.scheduler.advanceUntilIdle()
                mockAppLinkFlow.emit("https://test.redirect.org?state=$state")
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - the offline credential is asked for again before entering the app, which
                // is also what replaces a forgotten one
                val renewedState = expectMostRecentItem()
                assertIs<AfterLoginAction.CreateOfflineCredential>(
                    renewedState.afterLoginActions.firstOrNull(),
                )

                // AND - storing it with the SDK clears the gate
                whenever(setOAuthPin("5678")) doReturn Result.success(Unit)
                viewModel.onOfflineCredentialCreated("5678")
                testDispatcher.scheduler.advanceUntilIdle()

                verify(setOAuthPin).invoke("5678")
                val finalState = expectMostRecentItem()
                assertTrue(
                    finalState.afterLoginActions.none {
                        it is AfterLoginAction.CreateOfflineCredential
                    },
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GIVEN an offline login WHEN it succeeds THEN no offline credential is requested`() =
        runTest {
            // GIVEN - the user opens the account with the offline credential they already have
            val serverUrl = "https://test.server.org"
            val username = "testUser"
            val pin = "1234"

            whenever(getAvailableUsernames()) doReturn emptyList()
            whenever(getBiometricInfo(any())) doReturn BiometricsInfo(false, false)
            whenever(getHasOtherAccounts.invoke()) doReturn false
            whenever(getIsSessionLockedUseCase(true)) doReturn true
            whenever(loginUserOfflineWithCode.invoke(serverUrl, username, pin)) doReturn
                LoginResult.Success(displayTrackingMessage = false, initialSyncDone = true)

            initViewModel(
                serverUrl = serverUrl,
                username = username,
                entryMode = CredentialsEntryMode.EXISTING_OAUTH,
            )

            viewModel.credentialsScreenState.test(timeout = turbineTimeout) {
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN
                viewModel.onOfflineCredentialEntered(pin)
                testDispatcher.scheduler.advanceUntilIdle()
                testDispatcher.scheduler.advanceTimeBy(4.seconds)
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN - nothing was renewed, so the credential in use stays as it is
                val loggedInState = expectMostRecentItem()
                assertTrue(
                    loggedInState.afterLoginActions.none {
                        it is AfterLoginAction.CreateOfflineCredential
                    },
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun initViewModel(
        serverName: String? = "Test Server",
        serverUrl: String = "https://test.server.org",
        username: String? = null,
        allowRecovery: Boolean = true,
        entryMode: CredentialsEntryMode = CredentialsEntryMode.NEW_ACCOUNT_BASIC,
        autoPromptLogin: Boolean = true,
        oidcInfo: OidcInfo? = null,
        appLinkNavigation: AppLinkNavigation = this.appLinkNavigation,
    ): CredentialsViewModel {
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
                getOAuthLogoutUrl,
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
                entryMode = entryMode,
                autoPromptLogin = autoPromptLogin,
                setOAuthPin = setOAuthPin,
                loginUserOfflineWithCode = loginUserOfflineWithCode,
                credentialsResourceProvider = credentialsResourceProvider,
                getSessionRenewalUrl = getSessionRenewalUrl,
            )
        return viewModel
    }
}
