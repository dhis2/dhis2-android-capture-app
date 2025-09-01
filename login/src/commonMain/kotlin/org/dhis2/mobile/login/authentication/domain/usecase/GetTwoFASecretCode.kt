package org.dhis2.mobile.login.authentication.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository

class GetTwoFASecretCode(
    private val twoFARepository: TwoFARepository,
) {
    operator fun invoke(): Flow<String> = twoFARepository.getTwoFASecretCode()
}
