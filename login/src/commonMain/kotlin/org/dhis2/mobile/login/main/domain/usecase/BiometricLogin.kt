package org.dhis2.mobile.login.main.domain.usecase

import coil3.PlatformContext
import org.dhis2.mobile.login.main.data.LoginRepository

class BiometricLogin(
    private val repository: LoginRepository,
) {
    context(context: PlatformContext)
    suspend operator fun invoke() = repository.loginWithBiometric()
}
