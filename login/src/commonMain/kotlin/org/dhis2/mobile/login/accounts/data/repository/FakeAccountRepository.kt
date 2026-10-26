package org.dhis2.mobile.login.accounts.data.repository

import org.dhis2.mobile.login.accounts.domain.model.AccountModel

/**
 * [AccountRepository] for targets without the DHIS2 Android SDK: there are no stored accounts.
 */
internal class FakeAccountRepository : AccountRepository {
    override suspend fun getLoggedInAccounts(): List<AccountModel> = emptyList()

    override suspend fun availableServers(): List<String> = emptyList()

    override suspend fun getActiveAccount(): AccountModel? = null
}
