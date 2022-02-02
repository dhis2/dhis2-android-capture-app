package org.dhis2.usescases.login.accounts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.dhis2.usescases.login.accounts.ui.AccountsScreen

class AccountsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountsScreen(
                accounts = listOf(),
                onAccountClicked = {},
                onAddAccountClicked = {}
            )
        }
    }

    companion object {
        const val MAX_ACCOUNTS = 3
    }
}