package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository

class GetDeviceEnrollmentUrl(
    private val repository: LoginRepository,
) : UseCase<String, String> {
    override suspend fun invoke(input: String): Result<String> =
        try {
            Result.success(repository.getDeviceEnrollmentUrl(input))
        } catch (e: DomainError) {
            Result.failure(e)
        }
}
