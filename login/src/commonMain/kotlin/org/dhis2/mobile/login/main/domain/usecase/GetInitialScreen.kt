package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.main.domain.model.LoginScreenState

class GetInitialScreen(
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(): LoginScreenState {
        val accounts = accountRepository.getLoggedInAccounts()
        return when {
            accounts.isEmpty() ->
                LoginScreenState.ServerValidation(
                    currentServer = "",
                    availableServers = accountRepository.availableServers(),
                )
            accounts.size == 1 ->
                LoginScreenState.LegacyLogin(
                    selectedServer = accounts.first().serverUrl,
                    selectedUsername = accounts.first().name,
                )

            else -> LoginScreenState.Accounts
        }
    }
}
