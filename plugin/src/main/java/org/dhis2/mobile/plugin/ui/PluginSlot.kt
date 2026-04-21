package org.dhis2.mobile.plugin.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.security.ScopedDhis2PluginContextFactory
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.LocalResourceReader
import org.koin.compose.koinInject

/**
 * Renders all plugins registered for [injectionPoint].
 *
 * Place this Composable at any injection point in the host UI to make it extensible.
 * Plugins render in declaration order with no extra spacing — plugins are responsible
 * for their own padding.
 *
 * CMP Resources: each plugin's composition is wrapped with a
 * [CompositionLocalProvider] that installs a filesystem-backed [FileSystemResourceReader]
 * pointing at the plugin's extracted `{resourceRoot}/composeResources/…` directory.
 * That intercepts the `LocalResourceReader` used by CMP's `stringResource` /
 * `painterResource` / `imageResource` — so each plugin reads its own strings,
 * drawables, and fonts without touching the host's AssetManager.
 *
 * Note: Plugin Composables run in the same process and composition scope as the host
 * app. A crash inside a plugin will propagate to the enclosing composition.
 */
@Composable
fun PluginSlot(
    injectionPoint: InjectionPoint,
    pluginRegistry: PluginRegistry = koinInject(),
    contextFactory: ScopedDhis2PluginContextFactory = koinInject(),
) {
    val plugins by pluginRegistry.plugins.collectAsState()
    val slotPlugins = plugins.filter { injectionPoint in it.plugin.metadata.injectionPoints }

    slotPlugins.forEach { registered ->
        key(registered.plugin.metadata.id) {
            PluginContent(registered = registered, contextFactory = contextFactory)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PluginContent(
    registered: RegisteredPlugin,
    contextFactory: ScopedDhis2PluginContextFactory,
) {
    val reader = remember(registered.resourceRoot) {
        FileSystemResourceReader(registered.resourceRoot)
    }
    CompositionLocalProvider(LocalResourceReader provides reader) {
        val context = contextFactory.create(registered.plugin.metadata)
        registered.plugin.content(context)
    }
}
