package org.dhis2.mobile.login.main.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.biometrics.CryptographicActions
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.commons.reporting.AnalyticActions
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.dhis2.mobile.login.authentication.OpenIdController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.AuthorizationType
import org.hisp.dhis.android.core.configuration.internal.DatabaseAccount
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.user.oauth2.OAuth2Config
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class LoginRepositoryImplTest {
    private companion object {
        const val PIN = "1234"
    }

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val domainErrorMapper: DomainErrorMapper = mock()
    private val loginErrorMessageProvider: LoginErrorMessageProvider = mock()
    private val testDispatcher = StandardTestDispatcher()

    private val serverUrl = "https://test.server.org"
    private val authorizationUrl = "$serverUrl/oauth2/authorize"
    private val enrollmentUrl = "$serverUrl/api/auth/enrollDevice"
    private val urlErrorMessage = "The session could not be renewed"

    private val oauth2Handler get() = d2.userModule().oauth2Handler()
    private val openIdHandler get() = d2.userModule().openIdHandler()

    private val repository =
        LoginRepositoryImpl(
            d2 = d2,
            authenticator = mock<BiometricActions>(),
            cryptographyManager = mock<CryptographicActions>(),
            preferences = mock<PreferenceProvider>(),
            d2ErrorMessageProvider = mock<D2ErrorMessageProvider>(),
            crashReportController = mock<CrashReportController>(),
            analyticActions = mock<AnalyticActions>(),
            openIdController = mock<OpenIdController>(),
            dispatcher = Dispatcher(testDispatcher, testDispatcher, testDispatcher),
            domainErrorMapper = domainErrorMapper,
            loginErrorMessageProvider = loginErrorMessageProvider,
        )

    @BeforeTest
    fun setUp() {
        // runTest reuses the scheduler of the main dispatcher, so the repository and the test
        // share one clock
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a device holding a client registration WHEN it is checked THEN it is reported as registered`() =
        runTest {
            // GIVEN
            whenever(oauth2Handler.isDeviceRegistered()) doReturn true

            // WHEN & THEN - this is what tells a session renewal apart from a first login
            assertTrue(repository.isDeviceRegistered())
        }

    @Test
    fun `GIVEN a device with no client registration WHEN it is checked THEN it is reported as not registered`() =
        runTest {
            // GIVEN
            whenever(oauth2Handler.isDeviceRegistered()) doReturn false

            // WHEN & THEN
            assertFalse(repository.isDeviceRegistered())
        }

    @Test
    fun `GIVEN a registered device WHEN the authorization url is requested THEN it is built for that server`() =
        runTest {
            // GIVEN
            whenever(
                oauth2Handler.blockingLogIn(OAuth2Config(serverUrl = serverUrl)),
            ) doReturn authorizationUrl

            // WHEN
            val url = repository.getAuthorizationUrl(serverUrl)

            // THEN - the config carries the server being renewed, with the SDK defaults for the
            // redirect uri and scope
            assertEquals(authorizationUrl, url)
            verify(oauth2Handler).blockingLogIn(OAuth2Config(serverUrl = serverUrl))
        }

    @Test
    fun `GIVEN the SDK rejects the authorization WHEN the url is requested THEN the mapped error is thrown`() =
        runTest {
            // GIVEN
            val d2Error = d2Error(D2ErrorCode.OAUTH2_DEVICE_NOT_REGISTERED)
            val mappedError = DomainError.AuthenticationError("Device not registered")
            whenever(
                oauth2Handler.blockingLogIn(OAuth2Config(serverUrl = serverUrl)),
            ).thenAnswer { throw d2Error }
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn mappedError

            // WHEN
            val thrown = assertFailsWithDomainError { repository.getAuthorizationUrl(serverUrl) }

            // THEN - the domain layer sees the mapped error, not the SDK one
            assertEquals(mappedError, thrown)
        }

    @Test
    fun `GIVEN no stored authorization endpoint WHEN the url is requested THEN a configuration error is thrown`() =
        runTest {
            // GIVEN - the SDK reports this outside D2Error, which would otherwise reach the UI as
            // a crash instead of a message
            whenever(
                oauth2Handler.blockingLogIn(OAuth2Config(serverUrl = serverUrl)),
            ).thenAnswer { throw IllegalStateException("No authorization endpoint stored") }
            whenever(loginErrorMessageProvider.oauthUrlError()) doReturn urlErrorMessage

            // WHEN
            val thrown = assertFailsWithDomainError { repository.getAuthorizationUrl(serverUrl) }

            // THEN
            assertEquals(DomainError.ConfigurationError(urlErrorMessage), thrown)
        }

    @Test
    fun `GIVEN the call is cancelled WHEN the authorization url is requested THEN cancellation is not swallowed`() =
        runTest {
            // GIVEN
            val cancellation = CancellationException("cancelled")
            whenever(
                oauth2Handler.blockingLogIn(OAuth2Config(serverUrl = serverUrl)),
            ).thenAnswer { throw cancellation }

            // WHEN & THEN - reporting it as a configuration error would turn a cancelled coroutine
            // into a message for the user
            try {
                repository.getAuthorizationUrl(serverUrl)
                fail("Expected the cancellation to propagate")
            } catch (e: CancellationException) {
                assertEquals(cancellation.message, e.message)
            }
        }

    @Test
    fun `GIVEN a server with OAuth2 WHEN the enrollment url is requested THEN it is built for that server`() =
        runTest {
            // GIVEN
            whenever(oauth2Handler.blockingBuildEnrollmentUrl(serverUrl)) doReturn enrollmentUrl

            // WHEN & THEN
            assertEquals(enrollmentUrl, repository.getDeviceEnrollmentUrl(serverUrl))
        }

    @Test
    fun `GIVEN the SDK rejects the enrollment WHEN the url is requested THEN the mapped error is thrown`() =
        runTest {
            // GIVEN
            val d2Error = d2Error(D2ErrorCode.OAUTH2_INCOMPLETE_REGISTRATION)
            val mappedError = DomainError.AuthenticationError("OAuth2 error")
            whenever(oauth2Handler.blockingBuildEnrollmentUrl(serverUrl))
                .thenAnswer { throw d2Error }
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn mappedError

            // WHEN
            val thrown = assertFailsWithDomainError { repository.getDeviceEnrollmentUrl(serverUrl) }

            // THEN
            assertEquals(mappedError, thrown)
        }

    @Test
    fun `GIVEN an unusable OAuth2 configuration WHEN the enrollment url is requested THEN a configuration error is thrown`() =
        runTest {
            // GIVEN - as with the authorization url, the SDK reports this outside D2Error
            whenever(oauth2Handler.blockingBuildEnrollmentUrl(serverUrl))
                .thenAnswer { throw IllegalStateException("No enrollment endpoint stored") }
            whenever(loginErrorMessageProvider.oauthUrlError()) doReturn urlErrorMessage

            // WHEN
            val thrown = assertFailsWithDomainError { repository.getDeviceEnrollmentUrl(serverUrl) }

            // THEN
            assertEquals(DomainError.ConfigurationError(urlErrorMessage), thrown)
        }

    @Test
    fun `GIVEN the call is cancelled WHEN the enrollment url is requested THEN cancellation is not swallowed`() =
        runTest {
            // GIVEN
            val cancellation = CancellationException("cancelled")
            whenever(oauth2Handler.blockingBuildEnrollmentUrl(serverUrl))
                .thenAnswer { throw cancellation }

            // WHEN & THEN
            try {
                repository.getDeviceEnrollmentUrl(serverUrl)
                fail("Expected the cancellation to propagate")
            } catch (e: CancellationException) {
                assertEquals(cancellation.message, e.message)
            }
        }

    @Test
    fun `GIVEN an OpenID account WHEN the offline pin is stored THEN the OpenID handler keeps it`() =
        runTest {
            // GIVEN - each SDK handler refuses the pin unless its own state is in the credentials,
            // so the account's authorization method decides which one is asked
            givenActiveAccountWith(AuthorizationType.OPEN_ID_CONNECT)
            whenever(openIdHandler.suspendSetPin(PIN)) doReturn Result.Success(Unit)

            // WHEN
            val result = repository.setOfflinePin(PIN)

            // THEN
            assertTrue(result.isSuccess)
            verify(openIdHandler).suspendSetPin(PIN)
            verify(oauth2Handler, never()).suspendSetPin(PIN)
        }

    @Test
    fun `GIVEN an OAuth2 account WHEN the offline pin is stored THEN the OAuth2 handler keeps it`() =
        runTest {
            // GIVEN
            givenActiveAccountWith(AuthorizationType.OAUTH2)
            whenever(oauth2Handler.suspendSetPin(PIN)) doReturn Result.Success(Unit)

            // WHEN
            val result = repository.setOfflinePin(PIN)

            // THEN
            assertTrue(result.isSuccess)
            verify(oauth2Handler).suspendSetPin(PIN)
            verify(openIdHandler, never()).suspendSetPin(PIN)
        }

    @Test
    fun `GIVEN the handler rejects the pin WHEN it is stored THEN the mapped error is returned`() =
        runTest {
            // GIVEN
            val d2Error = d2Error(D2ErrorCode.NO_AUTHENTICATED_USER)
            val mappedError = DomainError.AuthenticationError("There is no active session")
            givenActiveAccountWith(AuthorizationType.OPEN_ID_CONNECT)
            whenever(openIdHandler.suspendSetPin(PIN)) doReturn Result.Failure(d2Error)
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn mappedError

            // WHEN
            val result = repository.setOfflinePin(PIN)

            // THEN - the caller logs the user out on failure, so the reason has to survive
            assertEquals(mappedError, result.exceptionOrNull())
        }

    private fun givenActiveAccountWith(authorizationType: AuthorizationType) {
        val account: DatabaseAccount = mock()
        whenever(account.authorizationType) doReturn authorizationType
        whenever(d2.userModule().accountManager().getCurrentAccount()) doReturn account
    }

    private fun d2Error(errorCode: D2ErrorCode): D2Error =
        D2Error
            .builder()
            .errorCode(errorCode)
            .errorDescription(errorCode.name)
            .build()

    /** DomainError extends Throwable, so it is not caught by assertFailsWith<Exception>. */
    private inline fun assertFailsWithDomainError(block: () -> Unit): DomainError {
        try {
            block()
        } catch (e: DomainError) {
            return e
        }
        fail("Expected a DomainError to be thrown")
    }
}
