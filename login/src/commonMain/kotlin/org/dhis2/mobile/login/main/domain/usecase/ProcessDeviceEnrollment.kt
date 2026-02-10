package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.DeviceEnrollmentInfo

class ProcessDeviceEnrollment(
    private val repository: LoginRepository,
) : UseCase<DeviceEnrollmentInfo, String> {
    override suspend fun invoke(input: DeviceEnrollmentInfo): Result<String> =
        try {
            Result.success(
                repository.enrollDevice(
                    iat = input.iat,
                    serverURL = input.serverURL,
                ),
            )
        } catch (e: DomainError) {
            Result.failure(e)
        }
}
