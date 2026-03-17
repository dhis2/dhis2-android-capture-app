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

val pluginModule = module {
    // Infrastructure
    single { PluginRegistry() }
    singleOf(::PluginLoader)
    singleOf(::PluginVerifier)
    single { PluginDownloader(androidContext()) }

    // Data
    single { AppHubPluginRepository(get()) }

    // Security
    single { ScopedDhis2PluginContextFactory(get()) }

    // Domain
    factoryOf(::GetPluginSlotContent)
    factory {
        LoadPluginsUseCase(
            appHubPluginRepository = get(),
            pluginDownloader = get(),
            pluginVerifier = get(),
            pluginLoader = get(),
            pluginRegistry = get(),
            koin = getKoin(),
        )
    }
}
