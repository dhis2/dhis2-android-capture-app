package org.dhis2.mobile.extensibility.host.di

import kotlinx.coroutines.Dispatchers
import org.dhis2.mobile.extensibility.host.data.CustomFormRepository
import org.dhis2.mobile.extensibility.host.data.CustomFormRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module
import timber.log.Timber

internal actual val extensionHostPlatformModule: Module =
    module {
        factory<CustomFormRepository> { CustomFormRepositoryImpl(get(), Dispatchers.IO) }
        single {
            ExtensionHostConfig(
                log = { Timber.tag("ExtensionHost").w(it) },
            )
        }
    }
