package org.dhis2.mobile.login.pin.di

import org.dhis2.mobile.login.pin.data.SessionRepository
import org.dhis2.mobile.login.pin.data.SessionRepositoryImpl
import org.koin.dsl.module

/**
 * Android-specific PIN module providing repository implementation.
 */
internal actual val pinDataModule =
    module {
        factory<SessionRepository> {
            SessionRepositoryImpl(
                d2 = get(),
                preferenceProvider = get(),
                domainErrorMapper = get(),
                dispatcher = get(),
            )
        }
    }
