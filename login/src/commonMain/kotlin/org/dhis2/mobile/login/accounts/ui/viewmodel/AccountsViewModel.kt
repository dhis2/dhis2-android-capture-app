package org.dhis2.mobile.login.accounts.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.ui.navigation.Navigator

class AccountsViewModel(
    private val navigator: Navigator,
    private val repository: AccountRepository,
) : ViewModel() {
    private var _accounts = MutableStateFlow<List<AccountModel>>(emptyList())
    val accounts =
        _accounts
            .onStart {
                getAccounts()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList(),
            )

    private fun getAccounts() {
        viewModelScope.launch {
            _accounts.value = repository.getLoggedInAccounts()
        }
    }

    fun onAccountClicked(account: AccountModel) {
        viewModelScope.launch {
            navigator.navigate(
                LoginScreenState.LegacyLogin(
                    selectedServer = account.serverUrl,
                    selectedUsername = account.name,
                    serverName = account.serverName,
                    selectedServerFlag = account.serverFlag,
                    allowRecovery = account.allowRecovery,
                ),
            )
        }
    }

    fun onAddAccountClicked() {
        viewModelScope.launch {
            navigator.navigate(
                LoginScreenState.ServerValidation(
                    currentServer = "",
                    availableServers = repository.availableServers(),
                    hasAccounts = _accounts.value.isNotEmpty(),
                ),
            )
        }
    }
}
