package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository

class GetHasOtherAccounts(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke() = repository.hasOtherAccounts()
}
