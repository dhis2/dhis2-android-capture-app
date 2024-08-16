package org.dhis2.data.biometric

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity

@Module
object BiometricModule {

    @JvmStatic
    @Provides
    @PerActivity
    fun provideBiometricController(
        context: ActivityGlobalAbstract,
    ): BiometricController {
        return BiometricController(context)
    }
}
