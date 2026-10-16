package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.model.SessionRenewalRequest

/**
 * Builds the url that renews the session of an account already present on this device, by taking
 * the user through the browser to obtain new tokens.
 *
 * Enrollment and authorization are independent flows: the device registration outlives the
 * tokens, so a registered device only repeats the authorization one and the account database is
 * left untouched. Checking the server first is what stores the authorization endpoint the SDK
 * needs to build the url.
 */
class GetSessionRenewalUrl(
    private val repository: LoginRepository,
) : UseCase<SessionRenewalRequest, String> {
    override suspend fun invoke(input: SessionRenewalRequest): Result<String> =
        try {
            when (
                val validation =
                    repository.validateServer(input.serverUrl, input.isNetworkAvailable)
            ) {
                // The message already states why the server could not be checked (offline,
                // unreachable, not a DHIS2 server), so it is passed on as it is
                is ServerValidationResult.Error ->
                    Result.failure(DomainError.ServerError(validation.message))

                is ServerValidationResult.Success ->
                    Result.success(
                        if (repository.isDeviceRegistered()) {
                            repository.getAuthorizationUrl(input.serverUrl)
                        } else {
                            repository.getDeviceEnrollmentUrl(input.serverUrl)
                        },
                    )
            }
        } catch (e: DomainError) {
            Result.failure(e)
        }
}
