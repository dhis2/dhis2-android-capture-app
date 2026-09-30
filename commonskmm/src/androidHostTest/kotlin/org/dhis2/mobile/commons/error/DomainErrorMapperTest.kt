package org.dhis2.mobile.commons.error

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainErrorMapperTest {
    private val d2ErrorMessageProvider: D2ErrorMessageProvider = mock()
    private val networkStatusProvider: NetworkStatusProvider = mock()

    private val mapper = DomainErrorMapper(d2ErrorMessageProvider, networkStatusProvider)

    @Test
    fun `GIVEN an OAuth2 device not registered error WHEN it is mapped THEN it is an authentication error`() =
        runTest {
            // GIVEN - the SDK rejects the OAuth login because the device is not enrolled
            val deviceNotRegisteredMessage = "Device not registered"
            val d2Error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.OAUTH2_DEVICE_NOT_REGISTERED)
                    .errorDescription("Device is not registered")
                    .build()

            whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
            whenever(
                d2ErrorMessageProvider.getErrorMessage(d2Error, true),
            ) doReturn deviceNotRegisteredMessage

            // WHEN
            val domainError = mapper.mapToDomainError(d2Error)

            // THEN - the login layer receives an authentication error carrying the
            // device-specific message, not the generic OAuth2 one
            assertEquals(
                DomainError.AuthenticationError(deviceNotRegisteredMessage),
                domainError,
            )
        }

    @Test
    fun `GIVEN an OAuth2 incomplete registration error WHEN it is mapped THEN it is an authentication error`() =
        runTest {
            // GIVEN - the SDK rejects the enrollment because the device registration
            // was never completed
            val oauth2ErrorMessage = "There was an error when authenticating with OAuth2"
            val d2Error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.OAUTH2_INCOMPLETE_REGISTRATION)
                    .errorDescription("Device registration is incomplete")
                    .build()

            whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
            whenever(
                d2ErrorMessageProvider.getErrorMessage(d2Error, true),
            ) doReturn oauth2ErrorMessage

            // WHEN
            val domainError = mapper.mapToDomainError(d2Error)

            // THEN - the login layer receives an authentication error, so the user is sent
            // back to re-authenticate rather than seeing an unexpected-error dead end
            assertEquals(
                DomainError.AuthenticationError(oauth2ErrorMessage),
                domainError,
            )
        }

    @Test
    fun `GIVEN an OAuth2 no valid token error WHEN it is mapped THEN a session renewal is required`() =
        runTest {
            // GIVEN - the refresh token was rejected, so the session can no longer reach the server
            val noValidTokenMessage = "Log in again to keep syncing"
            val d2Error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.OAUTH2_NO_VALID_TOKEN)
                    .errorDescription("There is no valid OAuth2 token")
                    .build()

            whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
            whenever(
                d2ErrorMessageProvider.getErrorMessage(d2Error, true),
            ) doReturn noValidTokenMessage

            // WHEN
            val domainError = mapper.mapToDomainError(d2Error)

            // THEN - this is not a credentials problem: the account keeps working offline and the
            // app has to send the user through the login again, so it gets its own error
            assertEquals(
                DomainError.SessionRenewalRequiredError(noValidTokenMessage),
                domainError,
            )
        }

    @Test
    fun `GIVEN an OpenId no valid token error WHEN it is mapped THEN a session renewal is required`() =
        runTest {
            // GIVEN - the provider rejected the refresh token of an OpenId Connect account
            val noValidTokenMessage = "Log in again to keep syncing"
            val d2Error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.OPEN_ID_CONNECT_NO_VALID_TOKEN)
                    .errorDescription("There is no valid OpenId Connect token")
                    .build()

            whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
            whenever(
                d2ErrorMessageProvider.getErrorMessage(d2Error, true),
            ) doReturn noValidTokenMessage

            // WHEN
            val domainError = mapper.mapToDomainError(d2Error)

            // THEN - OpenId Connect shares the OAuth2 model once the account exists, so both
            // codes lead the app to the same session renewal prompt
            assertEquals(
                DomainError.SessionRenewalRequiredError(noValidTokenMessage),
                domainError,
            )
        }

    @Test
    fun `GIVEN a renewal authorized by another user WHEN it is mapped THEN it is an authentication error`() =
        runTest {
            // GIVEN - the browser session belonged to someone else, so the SDK refuses the login
            // before touching the account being restored
            val mismatchMessage = "You logged in with a different account"
            val d2Error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.AUTHENTICATED_USER_MISMATCH)
                    .errorDescription("The authorized user does not match the account being restored")
                    .build()

            whenever(networkStatusProvider.connectionStatus) doReturn flowOf(true)
            whenever(
                d2ErrorMessageProvider.getErrorMessage(d2Error, true),
            ) doReturn mismatchMessage

            // WHEN
            val domainError = mapper.mapToDomainError(d2Error)

            // THEN - nothing was replaced and no renewal is pending: the user simply has to try
            // again with the right account, so it is reported like any other credentials problem
            assertEquals(
                DomainError.AuthenticationError(mismatchMessage),
                domainError,
            )
        }
}
