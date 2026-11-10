package org.dhis2.mobile.extension.host.di

import android.content.Context
import android.content.pm.ApplicationInfo
import kotlinx.coroutines.Dispatchers
import org.dhis2.mobile.extension.host.data.CustomFormRepository
import org.dhis2.mobile.extension.host.data.SdkCustomFormRepository
import org.koin.core.module.Module
import org.koin.dsl.module
import timber.log.Timber

internal actual val extensionHostPlatformModule: Module =
    module {
        factory<CustomFormRepository> { SdkCustomFormRepository(get(), Dispatchers.IO) }
        single {
            ExtensionHostConfig(
                allowHttp = get<Context>().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
                log = { Timber.tag("ExtensionHost").w(it) },
            )
        }
    }
