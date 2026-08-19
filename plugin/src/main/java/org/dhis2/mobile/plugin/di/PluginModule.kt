package org.dhis2.mobile.plugin.di

import org.dhis2.mobile.plugin.data.AppHubPluginRepository
import org.dhis2.mobile.plugin.data.PluginDownloader
import org.dhis2.mobile.plugin.data.PluginLoader
import org.dhis2.mobile.plugin.data.PluginVerifier
import org.dhis2.mobile.plugin.domain.GetPluginSlotContent
import org.dhis2.mobile.plugin.domain.LoadPluginsUseCase
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.security.ScopedDhis2PluginContextFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val pluginModule =
    module {
        // Infrastructure
        single { PluginRegistry() }
        single { PluginLoader(androidContext()) }
        singleOf(::PluginVerifier)
        single { PluginDownloader(androidContext()) }

        // Data
        single { AppHubPluginRepository(get(), get()) }

        // Domain
        factoryOf(::GetPluginSlotContent)
        factory {
            LoadPluginsUseCase(
                appHubPluginRepository = get(),
                pluginDownloader = get(),
                pluginVerifier = get(),
                pluginLoader = get(),
                pluginRegistry = get(),
                // Constructed inline rather than registered as a definition. It holds the real D2
                // and mints a context from whatever metadata it is handed, so a plugin that could
                // resolve it from the container could hand it a scope granting everything.
                contextFactory = ScopedDhis2PluginContextFactory(get()),
            )
        }
    }
