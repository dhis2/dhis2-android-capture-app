package org.dhis2.mobile.login.authentication.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository

class GetTwoFAStatus(
    private val twoFARepository: TwoFARepository,
) {

    operator fun invoke(): Flow<TwoFAStatus> {
        return twoFARepository.getTwoFAStatus()
    }
}
