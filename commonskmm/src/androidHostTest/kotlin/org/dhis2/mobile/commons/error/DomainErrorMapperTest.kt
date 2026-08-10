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
}
