package org.dhis2.mobile.plugin.sdk

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

/**
 * Main entry point for a DHIS2 Android plugin.
 *
 * External developers implement this interface and declare the implementing class
 * in the plugin's App Hub metadata via the [PluginMetadata.entryPoint] field.
 *
 * The class must have a public no-argument constructor.
 */
interface Dhis2Plugin {
    /** Declares the plugin's identity, capabilities, and data scope. */
    val metadata: PluginMetadata

    /**
     * Optionally provide a Koin module with the plugin's own dependencies
     * (ViewModels, repositories, use cases, etc.).
     *
     * These bindings are loaded into the host app's Koin container at plugin load time.
     */
    fun provideKoinModule(): Module? = null

    /**
     * Renders the plugin's UI at the designated injection point.
     *
     * The plugin Composable is contained within its slot and must not attempt to
     * navigate outside of it. All DHIS2 data access must go through [Dhis2PluginContext].
     */
    @Composable
    fun content(context: Dhis2PluginContext)
}
