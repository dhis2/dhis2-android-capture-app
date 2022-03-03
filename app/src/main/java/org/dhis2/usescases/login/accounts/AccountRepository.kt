package org.dhis2.usescases.login.accounts

import org.hisp.dhis.android.core.D2

class AccountRepository(val d2: D2) {

    fun getLoggedInAccounts(): List<AccountModel> {
        return d2.userModule().accountManager().getAccounts().map {
            AccountModel(it.username(), it.serverUrl())
        }
    }
}
