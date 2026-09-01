package org.dhis2.mobile.plugin.sdk

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

/**
 * Main entry point for a DHIS2 Android plugin.
 *
 * External developers implement this interface — usually that means writing a single
 * [content] Composable and nothing else.
 *
 * The plugin declares no identity of its own. Its id, version, entry-point class name and
 * data scope all live in the server-side configuration the DHIS2 administrator writes (see
 * [PluginMetadata]); the host reads them from there to download and load the plugin, and passes
 * them back in via [Dhis2PluginContext.pluginMetadata]. That keeps a single source of truth and
 * means a plugin cannot grant itself access it was not configured for.
 *
 * The class must have a public no-argument constructor.
 */
interface Dhis2Plugin {
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
