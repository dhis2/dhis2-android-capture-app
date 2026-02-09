package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.pin.data.SessionRepository

class GetInitialScreen(
    private val accountRepository: AccountRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(): LoginScreenState {
        val accounts = accountRepository.getLoggedInAccounts()

        return when {
            accounts.isEmpty() ->
                LoginScreenState.ServerValidation(
                    currentServer = "",
                    availableServers = accountRepository.availableServers(),
                    hasAccounts = false,
                )

            accounts.size == 1 -> handleSingleAccount(accounts.first())
            sessionRepository.isSessionLocked() -> handleLockedSession()
            else -> LoginScreenState.Accounts
        }
    }

    private fun handleSingleAccount(account: AccountModel): LoginScreenState =
        LoginScreenState.LoginCredentials(
            selectedServer = account.serverUrl,
            selectedUsername = account.name,
            serverName = account.serverName,
            selectedServerFlag = account.serverFlag,
            allowRecovery = account.allowRecovery,
            oAuthEnabled = account.isOauthEnabled,
        )

    private suspend fun handleLockedSession(): LoginScreenState {
        val activeAccount = accountRepository.getActiveAccount() ?: return LoginScreenState.Accounts
        return LoginScreenState.LoginCredentials(
            selectedServer = activeAccount.serverUrl,
            selectedUsername = activeAccount.name,
            serverName = activeAccount.serverName,
            selectedServerFlag = activeAccount.serverFlag,
            allowRecovery = activeAccount.allowRecovery,
            oAuthEnabled = activeAccount.isOauthEnabled,
        )
    }
}
