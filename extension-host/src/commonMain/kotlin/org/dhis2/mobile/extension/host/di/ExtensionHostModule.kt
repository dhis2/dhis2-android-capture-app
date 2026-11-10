package org.dhis2.mobile.extension.host.di

import org.dhis2.mobile.extension.host.domain.LegacyApiScan
import org.dhis2.mobile.extension.host.domain.ResolveCustomForm
import org.dhis2.mobile.extension.host.reporting.ExtensionFailureReporter
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Needs a CustomFormRepository (platform module below) and a CustomFormRenderer (the WebView runtime, or a fake).
 */
val extensionHostModule =
    module {
        includes(extensionHostPlatformModule)
        // Log only; the Sentry reporter replaces this binding.
        factory<ExtensionFailureReporter> {
            val log = get<ExtensionHostConfig>().log
            ExtensionFailureReporter { extension, reason -> log("extension ${extension.id} failed: $reason") }
        }
        factory {
            ResolveCustomForm(
                repository = get(),
                renderer = get(),
                rules = listOf(LegacyApiScan()),
                reporter = get(),
                allowHttp = get<ExtensionHostConfig>().allowHttp,
            )
        }
    }

internal expect val extensionHostPlatformModule: Module
