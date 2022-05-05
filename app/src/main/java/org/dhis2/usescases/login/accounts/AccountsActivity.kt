package org.dhis2.usescases.login.accounts

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.livedata.observeAsState
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.accounts.ui.AccountsScreen
import org.dhis2.utils.Constants

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
                onAddAccountClicked = { navigateToLogin() }
            )
        }
        viewModel.getAccounts()
    }

    private fun navigateToLogin(accountModel: AccountModel? = null) {
        val intent = Intent()
        val wasAccountClicked = accountModel?.let { true } ?: false
        accountModel?.let {
            intent.apply {
                putExtra(Constants.SERVER, it.serverUrl)
                putExtra(Constants.USER, it.name)
            }
        }
        intent.putExtra(Constants.ACCOUNT_USED, wasAccountClicked)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
    }
}
