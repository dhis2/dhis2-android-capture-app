package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository

class VerifyNeedOfflinePin(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isUserLogged() && repository.needsOfflinePin() && !repository.isPinStored()
}
