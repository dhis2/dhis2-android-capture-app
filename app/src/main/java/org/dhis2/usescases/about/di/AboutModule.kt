package org.dhis2.usescases.about.di

import org.dhis2.usescases.about.AboutViewModel
import org.dhis2.usescases.about.data.AboutRepositoryImpl
import org.dhis2.usescases.about.domain.AboutRepository
import org.dhis2.usescases.about.domain.GetAboutInfo
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val aboutModule =
    module {

        factory<AboutRepository> {
            AboutRepositoryImpl(
                d2 = get(),
                dispatcher = get(),
                domainErrorMapper = get(),
            )
        }

        factory {
            GetAboutInfo(get())
        }

        viewModel {
            AboutViewModel(
                getAboutInfo = get(),
                networkStatusProvider = get(),
            )
        }
    }
