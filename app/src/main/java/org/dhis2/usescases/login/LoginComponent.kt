package org.dhis2.usescases.login


import org.dhis2.data.dagger.PerActivity

import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [LoginModule::class])
interface LoginComponent {
    fun inject(loginActivity: LoginActivity)
}
