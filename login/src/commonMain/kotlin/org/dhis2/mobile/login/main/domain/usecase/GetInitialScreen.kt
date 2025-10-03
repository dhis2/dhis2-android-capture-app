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
            accounts.size == 1 && accounts.first().isOauthEnabled ->
                LoginScreenState.OauthLogin(
                    selectedServer = accounts.first().serverUrl,
                )

            accounts.size == 1 ->
                with(accounts.first()) {
                    LoginScreenState.LegacyLogin(
                        selectedServer = serverUrl,
                        selectedUsername = name,
                        serverName = serverName,
                        selectedServerFlag = serverFlag,
                        allowRecovery = allowRecovery,
                        oidcIcon = oidcIcon,
                        oidcLoginText = oidcLoginText,
                        oidcUrl = oidcUrl,
                    )
                }

            else -> LoginScreenState.Accounts
        }
    }
}
