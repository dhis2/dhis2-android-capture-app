package org.dhis2.mobile.extension.host.di

import org.dhis2.mobile.extension.host.domain.LegacyApiScan
import org.dhis2.mobile.extension.host.domain.NoFormScripts
import org.dhis2.mobile.extension.host.domain.ResolveCustomForm
import org.dhis2.mobile.extension.host.reporting.ExtensionFailureReporter
import org.dhis2.mobile.extension.host.reporting.SentryExtensionFailureReporter
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Needs a CustomFormRepository (platform module below) and a CustomFormRenderer (the WebView runtime, or a fake).
 */
val extensionHostModule =
    module {
        includes(extensionHostPlatformModule)
        factory<ExtensionFailureReporter> { SentryExtensionFailureReporter(get(), get<ExtensionHostConfig>().log) }
        factory {
            val config = get<ExtensionHostConfig>()
            ResolveCustomForm(
                repository = get(),
                renderer = get(),
                rules = if (config.legacyShim) listOf(LegacyApiScan()) else listOf(NoFormScripts(), LegacyApiScan()),
                reporter = get(),
                allowHttp = config.allowHttp,
                onSurveyed = { dataSetUid, survey -> config.log("Custom form of $dataSetUid: $survey") },
            )
        }
    }

internal expect val extensionHostPlatformModule: Module
