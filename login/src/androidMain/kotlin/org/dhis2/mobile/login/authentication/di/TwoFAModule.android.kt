package org.dhis2.mobile.login.authentication.di

import org.dhis2.mobile.login.authentication.data.repository.TwoFARepositoryImpl
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val twoFARepositoryModule: Module =
    module {
        factory<TwoFARepository> {
            TwoFARepositoryImpl(
                get(),
                get(),
            )
        }
    }
