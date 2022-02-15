package org.dhis2.usescases.login.accounts

import org.hisp.dhis.android.core.D2

class AccountRepository(val d2: D2) {

    fun getLoggedInAccounts(): List<AccountModel> {
        return listOf(
            AccountModel("userName 1", "serverUrl 1"),
            AccountModel("userName 2", "serverUrl 2")
        )
    }
}
