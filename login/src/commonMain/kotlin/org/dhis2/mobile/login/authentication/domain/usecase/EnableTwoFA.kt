package org.dhis2.mobile.login.authentication.domain.usecase

import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository

class EnableTwoFA(
    private val twoFARepository: TwoFARepository,
) {
    suspend operator fun invoke(
        code: String,
        isNetworkAvailable: Boolean,
    ) = twoFARepository.enableTwoFA(code, isNetworkAvailable)
}
