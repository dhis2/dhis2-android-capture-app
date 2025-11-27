package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.login.pin.data.SessionRepository

class GetIsSessionLockedUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke() = sessionRepository.isSessionLocked()
}
