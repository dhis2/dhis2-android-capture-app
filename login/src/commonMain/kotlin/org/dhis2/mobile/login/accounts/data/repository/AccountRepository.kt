package org.dhis2.mobile.login.accounts.data.repository

import org.dhis2.mobile.login.accounts.domain.model.AccountModel

interface AccountRepository {
    suspend fun getLoggedInAccounts(): List<AccountModel>

    suspend fun availableServers(): List<String>

    suspend fun getActiveAccount(): AccountModel?
}
