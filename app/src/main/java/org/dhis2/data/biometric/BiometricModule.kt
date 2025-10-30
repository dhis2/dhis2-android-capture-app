package org.dhis2.data.biometric

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.biometrics.CryptographicActions
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.koin.dsl.module

@Module
object BiometricModule {
    @JvmStatic
    @Provides
    @PerActivity
    fun provideBiometricController(context: ActivityGlobalAbstract): BiometricAuthenticator = BiometricAuthenticator(context)
}

val biometricModule =
    module {

        factory<BiometricActions> {
            BiometricAuthenticator(get())
        }

        factory<CryptographicActions> {
            CryptographyManager()
        }
    }
