package org.dhis2.mobile.plugin.di

import org.dhis2.mobile.plugin.api.PluginApiRouter
import org.dhis2.mobile.plugin.data.AppHubRepository
import org.dhis2.mobile.plugin.data.CaptureConfigRepository
import org.dhis2.mobile.plugin.data.FormPluginContextRepository
import org.dhis2.mobile.plugin.data.PluginAppRepository
import org.dhis2.mobile.plugin.data.PluginBundleInstaller
import org.dhis2.mobile.plugin.data.PluginFieldMetadataRepository
import org.dhis2.mobile.plugin.data.PluginFieldValueRepository
import org.dhis2.mobile.plugin.domain.LoadPlugins
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File

/** Directory under `filesDir` holding the extracted bundles the WebView serves. */
internal const val WEB_PLUGINS_DIR = "web-plugins"

val pluginModule =
    module {
        single { PluginRegistry() }
        single { PluginBundleInstaller(File(androidContext().filesDir, WEB_PLUGINS_DIR), get()) }
        single { CaptureConfigRepository(get(), get()) }
        single { PluginAppRepository(get(), get()) }
        single { AppHubRepository(get(), get()) }
        single { PluginFieldMetadataRepository(get(), get()) }
        single { PluginFieldValueRepository(get(), get()) }
        single { FormPluginContextRepository(get(), get()) }
        single { PluginApiRouter(get(), get()) }
        factory { LoadPlugins(get(), get(), get(), get(), get()) }
    }
