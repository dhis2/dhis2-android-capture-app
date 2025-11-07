package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository

class LogOutUser(
    repository: LoginRepository,
) : BaseLogin(repository) {
    suspend operator fun invoke(): Result<Unit> = repository.logoutUser()
}
