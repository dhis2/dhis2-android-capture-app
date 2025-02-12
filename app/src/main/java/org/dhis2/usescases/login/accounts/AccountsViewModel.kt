package org.dhis2.usescases.login.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

class AccountsViewModel(
    val repository: AccountRepository,
) : ViewModel() {

    private var _accounts = MutableLiveData<List<AccountModel>>()
    val accounts: LiveData<List<AccountModel>>
        get() = _accounts

    fun getAccounts() {
        _accounts.value = repository.getLoggedInAccounts()
    }

    fun onImportDataBase(
        file: File,
        onSuccess: (AccountModel) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        viewModelScope.launch {
            val resultJob = async { repository.importDatabase(file) }

            val result = resultJob.await()

            result.fold(
                onSuccess = {
                    onSuccess(
                        AccountModel(it.username, it.serverUrl),
                    )
                },
                onFailure = {
                    onFailure(it)
                },
            )
        }
    }
}
