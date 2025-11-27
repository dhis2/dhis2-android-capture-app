package org.dhis2.mobile.login.authentication.domain.usecase

import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository

class GetTwoFAStatus(
    private val twoFARepository: TwoFARepository,
) {
    suspend operator fun invoke(): TwoFAStatus = twoFARepository.getTwoFAStatus()
}
