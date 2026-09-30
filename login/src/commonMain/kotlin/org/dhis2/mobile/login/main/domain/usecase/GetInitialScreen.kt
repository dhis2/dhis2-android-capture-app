package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.domain.model.CredentialsEntryMode
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.pin.data.SessionRepository

class GetInitialScreen(
    private val accountRepository: AccountRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(renewSession: Boolean = false): LoginScreenState {
        val accounts = accountRepository.getLoggedInAccounts()

        return when {
            accounts.isEmpty() ->
                LoginScreenState.ServerValidation(
                    currentServer = "",
                    availableServers = accountRepository.availableServers(),
                    hasAccounts = false,
                )

            accounts.size == 1 -> handleSingleAccount(accounts.first(), renewSession)
            sessionRepository.isSessionLocked() -> handleLockedSession(renewSession)
            else -> LoginScreenState.Accounts
        }
    }

    private fun handleSingleAccount(
        account: AccountModel,
        renewSession: Boolean,
    ): LoginScreenState =
        LoginScreenState.LoginCredentials(
            selectedServer = account.serverUrl,
            selectedUsername = account.name,
            serverName = account.serverName,
            selectedServerFlag = account.serverFlag,
            allowRecovery = account.allowRecovery,
            entryMode = CredentialsEntryMode.existing(account.authorizationMethod),
            autoPromptLogin = false,
            autoStartRenewal = renewSession,
        )

    private suspend fun handleLockedSession(renewSession: Boolean): LoginScreenState {
        val activeAccount = accountRepository.getActiveAccount() ?: return LoginScreenState.Accounts
        return LoginScreenState.LoginCredentials(
            selectedServer = activeAccount.serverUrl,
            selectedUsername = activeAccount.name,
            serverName = activeAccount.serverName,
            selectedServerFlag = activeAccount.serverFlag,
            allowRecovery = activeAccount.allowRecovery,
            entryMode = CredentialsEntryMode.existing(activeAccount.authorizationMethod),
            autoPromptLogin = false,
            autoStartRenewal = renewSession,
        )
    }
}
