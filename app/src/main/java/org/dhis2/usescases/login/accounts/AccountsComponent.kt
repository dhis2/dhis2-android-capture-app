package org.dhis2.usescases.login.accounts

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [AccountsModule::class])
interface AccountsComponent {
    fun inject(accountsActivity: AccountsActivity)
}
