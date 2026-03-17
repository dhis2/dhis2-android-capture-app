package org.dhis2.mobile.plugin.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.security.ScopedDhis2PluginContextFactory
import org.koin.compose.koinInject

/**
 * Renders all [Dhis2Plugin] instances registered for [injectionPoint].
 *
 * Place this Composable at any injection point in the host UI to make it extensible.
 * Each plugin is rendered in declaration order, separated by no additional spacing —
 * plugins are responsible for their own internal padding/margins.
 *
 * Each plugin is rendered with its own [key] so Compose can independently track and
 * recompose them. The plugin's [Dhis2PluginContext][org.dhis2.mobile.plugin.sdk.Dhis2PluginContext]
 * is scoped to its declared metadata (programs/datasets access only).
 *
 * Note: Plugin Composables run in the same process and composition scope as the host app.
 * A crash inside a plugin will propagate to the enclosing composition. Future work will
 * investigate `SubcomposeLayout`-based isolation.
 */
@Composable
fun PluginSlot(
    injectionPoint: InjectionPoint,
    pluginRegistry: PluginRegistry = koinInject(),
    contextFactory: ScopedDhis2PluginContextFactory = koinInject(),
) {
    val plugins by pluginRegistry.plugins.collectAsState()
    val slotPlugins = plugins.filter { injectionPoint in it.metadata.injectionPoints }

    slotPlugins.forEach { plugin ->
        key(plugin.metadata.id) {
            PluginContent(plugin = plugin, contextFactory = contextFactory)
        }
    }
}

@Composable
private fun PluginContent(
    plugin: Dhis2Plugin,
    contextFactory: ScopedDhis2PluginContextFactory,
) {
    val context = contextFactory.create(plugin.metadata)
    plugin.content(context)
}
