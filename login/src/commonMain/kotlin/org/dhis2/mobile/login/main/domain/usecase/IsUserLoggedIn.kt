package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.login.main.data.LoginRepository

class IsUserLoggedIn(
    private val repository: LoginRepository,
) : UseCase<Unit, Boolean> {
    override suspend fun invoke(input: Unit) = repository.isUserLoggedIn()
}
