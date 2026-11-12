package org.dhis2.mobile.extension.host.di

import org.dhis2.mobile.extension.host.domain.LegacyApiScan
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
            ResolveCustomForm(
                repository = get(),
                renderer = get(),
                rules = listOf(LegacyApiScan()),
                reporter = get(),
                allowHttp = get<ExtensionHostConfig>().allowHttp,
                onSurveyed = { dataSetUid, survey -> get<ExtensionHostConfig>().log("Custom form of $dataSetUid: $survey") },
            )
        }
    }

internal expect val extensionHostPlatformModule: Module
