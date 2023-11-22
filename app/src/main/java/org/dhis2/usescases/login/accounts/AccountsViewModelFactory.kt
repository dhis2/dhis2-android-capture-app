package org.dhis2.usescases.login.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class AccountsViewModelFactory(
    val repository: AccountRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AccountsViewModel(repository) as T
    }
}
