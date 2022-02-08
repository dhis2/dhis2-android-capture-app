package org.dhis2.usescases.login.accounts

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
        var intent: Intent? = null
        accountModel?.let {
            intent = Intent().apply {
                putExtra(Constants.SERVER, it.serverUrl)
                putExtra(Constants.USER, it.name)
            }
        }
        setResult(RESULT_ACCOUNT, intent)
        finish()
    }

    companion object {
        const val MAX_ACCOUNTS = 3
        const val RESULT_ACCOUNT = 133
    }
}
