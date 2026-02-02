package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository

class ProcessDeviceEnrollment(
    private val repository: LoginRepository,
) : UseCase<String, String> {
    override suspend fun invoke(input: String): Result<String> =
        try {
            Result.success(repository.enrollDevice(input))
        } catch (e: DomainError) {
            Result.failure(e)
        }
}
