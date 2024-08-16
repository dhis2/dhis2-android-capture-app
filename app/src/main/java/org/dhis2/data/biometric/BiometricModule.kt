package org.dhis2.data.biometric

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract

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
