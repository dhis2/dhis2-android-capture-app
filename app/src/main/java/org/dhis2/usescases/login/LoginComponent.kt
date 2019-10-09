package org.dhis2.usescases.login


import org.dhis2.data.dagger.PerActivity

import dagger.Subcomponent
import org.dhis2.usescases.login.fingerprint.FingerPrintModule

@PerActivity
@Subcomponent(modules = [LoginModule::class, FingerPrintModule::class])
interface LoginComponent {
    fun inject(loginActivity: LoginActivity)
}
