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
        if (account.isOauthEnabled) {
            LoginScreenState.OauthLogin(selectedServer = account.serverUrl)
        } else {
            account.toLegacyLoginState()
        }

    private suspend fun handleLockedSession(): LoginScreenState {
        val activeAccount = accountRepository.getActiveAccount() ?: return LoginScreenState.Accounts

        return if (activeAccount.isOauthEnabled) {
            LoginScreenState.OauthLogin(selectedServer = activeAccount.serverUrl)
        } else {
            activeAccount.toLegacyLoginState()
        }
    }

    private fun AccountModel.toLegacyLoginState(): LoginScreenState.LegacyLogin =
        LoginScreenState.LegacyLogin(
            selectedServer = serverUrl,
            selectedUsername = name,
            serverName = serverName,
            selectedServerFlag = serverFlag,
            allowRecovery = allowRecovery,
        )
}
