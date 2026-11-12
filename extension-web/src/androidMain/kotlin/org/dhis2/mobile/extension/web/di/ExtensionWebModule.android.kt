package org.dhis2.mobile.extension.web.di

import org.dhis2.mobile.extension.web.api.ExtensionApiRouter
import org.koin.core.module.Module
import org.koin.dsl.module

actual val extensionWebModule: Module =
    module {
        single { ExtensionApiRouter(get(), get()) }
    }
