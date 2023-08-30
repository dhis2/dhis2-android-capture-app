package org.dhis2.usescases.login.accounts

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.livedata.observeAsState
import org.dhis2.bindings.app
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.login.accounts.ui.AccountsScreen
import javax.inject.Inject

class AccountsActivity : ActivityGlobalAbstract() {

    @Inject
    lateinit var viewModelFactory: AccountsViewModelFactory
    private val viewModel: AccountsViewModel by viewModels { viewModelFactory }

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app().serverComponent()?.plus(AccountsModule())?.inject(this)

        setContent {
            val accounts = viewModel.accounts.observeAsState(listOf())
            AccountsScreen(
                accounts = accounts.value,
                onAccountClicked = { navigateToLogin(it) },
                onAddAccountClicked = { navigateToLogin() },
            )
        }
        viewModel.getAccounts()
    }

    private fun navigateToLogin(accountModel: AccountModel? = null) {
        val wasAccountClicked = accountModel?.let { true } ?: false
        val intent = LoginActivity.accountIntentResult(
            serverUrl = accountModel?.serverUrl,
            userName = accountModel?.name,
            wasAccountClicked = wasAccountClicked,
        )
        setResult(RESULT_OK, intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
    }
}
