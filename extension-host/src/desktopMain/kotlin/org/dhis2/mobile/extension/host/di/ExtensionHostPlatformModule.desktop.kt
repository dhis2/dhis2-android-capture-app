package org.dhis2.mobile.extension.host.di

import org.dhis2.mobile.extension.host.data.CustomFormRepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val extensionHostPlatformModule: Module =
    module {
        single { ExtensionHostConfig(allowHttp = false, log = {}) }
        factory<CustomFormRepository> {
            object : CustomFormRepository {
                override suspend fun customFormHtml(dataSetUid: String): String? = null

                override suspend fun serverUsesHttps() = false
            }
        }
    }
