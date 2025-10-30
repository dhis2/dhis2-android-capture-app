package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.BiometricsInfo

class GetBiometricInfo(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(serverUrl: String): BiometricsInfo =
        BiometricsInfo(
            canUseBiometrics = repository.canLoginWithBiometrics(serverUrl),
            displayBiometricsMessageAfterLogin = repository.displayBiometricMessage(),
        )
}
