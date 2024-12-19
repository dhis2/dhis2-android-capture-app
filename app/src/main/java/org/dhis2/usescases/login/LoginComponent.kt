package org.dhis2.usescases.login

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.data.biometric.BiometricModule

@PerActivity
@Subcomponent(modules = [LoginModule::class, BiometricModule::class])
interface LoginComponent {
    fun inject(loginActivity: LoginActivity)
}
