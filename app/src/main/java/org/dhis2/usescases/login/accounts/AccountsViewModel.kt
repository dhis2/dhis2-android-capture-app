package org.dhis2.usescases.login.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountsViewModel(
    val repository: AccountRepository,
) : ViewModel() {

    private var _accounts = MutableLiveData<List<AccountModel>>()
    val accounts: LiveData<List<AccountModel>>
        get() = _accounts

    fun getAccounts() {
        _accounts.value = repository.getLoggedInAccounts()
    }
}
