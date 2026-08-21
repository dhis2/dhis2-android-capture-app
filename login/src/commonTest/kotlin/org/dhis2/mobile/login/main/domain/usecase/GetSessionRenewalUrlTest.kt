package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.model.SessionRenewalRequest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetSessionRenewalUrlTest {
    private val repository: LoginRepository = mock()
    private lateinit var getSessionRenewalUrl: GetSessionRenewalUrl

    private val serverUrl = "https://test.server.org"
    private val request = SessionRenewalRequest(serverUrl = serverUrl, isNetworkAvailable = true)
    private val authorizationUrl = "$serverUrl/oauth2/authorize"
    private val enrollmentUrl = "$serverUrl/api/auth/enrollDevice"

    @Before
    fun setUp() {
        getSessionRenewalUrl = GetSessionRenewalUrl(repository)
    }

    @Test
    fun `GIVEN the device is registered WHEN the session is renewed THEN only the authorization url is built`() =
        runTest {
            // GIVEN - this device already completed the enrollment ceremony
            whenever(repository.validateServer(serverUrl, true)) doReturn serverValidationSuccess()
            whenever(repository.isDeviceRegistered()) doReturn true
            whenever(repository.getAuthorizationUrl(serverUrl)) doReturn authorizationUrl

            // WHEN
            val result = getSessionRenewalUrl(request)

            // THEN - re-login repeats the authorization ceremony only: the device is not enrolled
            // again, so the local database and the stored registration are left untouched
            assertEquals(authorizationUrl, result.getOrNull())
            verify(repository, never()).getDeviceEnrollmentUrl(serverUrl)
        }

    @Test
    fun `GIVEN the session is renewed WHEN the server is reachable THEN the server is checked before building the url`() =
        runTest {
            // GIVEN
            whenever(repository.validateServer(serverUrl, true)) doReturn serverValidationSuccess()
            whenever(repository.isDeviceRegistered()) doReturn true
            whenever(repository.getAuthorizationUrl(serverUrl)) doReturn authorizationUrl

            // WHEN
            getSessionRenewalUrl(request)

            // THEN - the server check is what stores the authorization endpoint. Without it the
            // SDK has no url to send the user to and fails outside of its own error type
            inOrder(repository) {
                verify(repository).validateServer(serverUrl, true)
                verify(repository).getAuthorizationUrl(serverUrl)
            }
        }

    @Test
    fun `GIVEN the device is not registered WHEN the session is renewed THEN the enrollment url is built`() =
        runTest {
            // GIVEN - no client registration exists for this device and server
            whenever(repository.validateServer(serverUrl, true)) doReturn serverValidationSuccess()
            whenever(repository.isDeviceRegistered()) doReturn false
            whenever(repository.getDeviceEnrollmentUrl(serverUrl)) doReturn enrollmentUrl

            // WHEN
            val result = getSessionRenewalUrl(request)

            // THEN - the enrollment ceremony has to run first
            assertEquals(enrollmentUrl, result.getOrNull())
            verify(repository, never()).getAuthorizationUrl(serverUrl)
        }

    @Test
    fun `GIVEN the server cannot be checked WHEN the session is renewed THEN no url is built`() =
        runTest {
            // GIVEN - the server check fails, typically because the device is offline
            val validationError = "Server not reachable"
            whenever(repository.validateServer(serverUrl, false)) doReturn
                ServerValidationResult.Error(validationError)

            // WHEN
            val result = getSessionRenewalUrl(request.copy(isNetworkAvailable = false))

            // THEN - the flow stops with the server message: opening a browser without stored
            // endpoints would fail later and outside of the mapped error types
            assertEquals(validationError, result.exceptionOrNull()?.message)
            verify(repository, never()).isDeviceRegistered()
            verify(repository, never()).getAuthorizationUrl(serverUrl)
            verify(repository, never()).getDeviceEnrollmentUrl(serverUrl)
        }

    @Test
    fun `GIVEN the authorization url cannot be built WHEN the session is renewed THEN the error is returned`() =
        runTest {
            // GIVEN - the SDK rejects the authorization request
            val domainError = DomainError.AuthenticationError("Device not registered")
            whenever(repository.validateServer(serverUrl, true)) doReturn serverValidationSuccess()
            whenever(repository.isDeviceRegistered()) doReturn true
            // DomainError extends Throwable, so it has to be thrown from an answer
            whenever(repository.getAuthorizationUrl(serverUrl)).thenAnswer { throw domainError }

            // WHEN
            val result = getSessionRenewalUrl(request)

            // THEN - the mapped error reaches the caller instead of escaping as a crash
            assertIs<DomainError.AuthenticationError>(result.exceptionOrNull())
        }

    private fun serverValidationSuccess() =
        ServerValidationResult.Success(
            serverName = "Test Server",
            serverDescription = null,
            countryFlag = null,
            allowRecovery = false,
            oidcIcon = null,
            oidcLoginText = null,
            oidcUrl = null,
            oAuthEnabled = true,
        )
}
