package org.dhis2.mobile.extension.web.di

import org.dhis2.mobile.extension.host.renderer.CustomFormRenderer
import org.dhis2.mobile.extension.web.api.ExtensionApiRouter
import org.dhis2.mobile.extension.web.customform.CustomFormDataRepository
import org.dhis2.mobile.extension.web.customform.WebViewCustomFormRenderer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val extensionWebModule: Module =
    module {
        single { ExtensionApiRouter(get(), get()) }
        single { CustomFormDataRepository(get(), get()) }
        factory<CustomFormRenderer> { WebViewCustomFormRenderer(get(), get(), get()) }
    }
