package org.dhis2.mobile.login.accounts.data.repository

import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.hisp.dhis.android.core.D2

class AccountRepositoryImpl(private val d2: D2) : AccountRepository {
    override suspend fun getLoggedInAccounts(): List<AccountModel> {
        return d2.userModule().accountManager().getAccounts().map {
            AccountModel(it.username(), it.serverUrl())
        }
    }
}
