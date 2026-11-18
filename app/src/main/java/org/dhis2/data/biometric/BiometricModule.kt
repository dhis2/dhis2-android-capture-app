package org.dhis2.data.biometric

import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.koin.dsl.module

val biometricModule =
    module {
        factory<BiometricActions> {
            BiometricAuthenticator(get(), CryptographyManager(), get())
        }
    }
