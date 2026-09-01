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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.LocalResourceReader
import org.koin.compose.KoinIsolatedContext
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
) {
    val plugins by pluginRegistry.plugins.collectAsState()
    val slotPlugins = plugins.filter { injectionPoint in it.metadata.injectionPoints }

    slotPlugins.forEach { registered ->
        // Keyed on the class loader as well as the id, because a reload replaces the registry entry
        // with a new InMemoryDexClassLoader and therefore new plugin classes. Keying on the id alone
        // kept the previous composition's `remember`ed slots alive across that swap, so state a
        // plugin had stored — a `produceState` value, for instance — was an instance of the *old*
        // loader's class while the new code cast it to the new loader's, giving
        // `ClassCastException: Foo cannot be cast to Foo`.
        key(registered.metadata.id, registered.classLoader) {
            PluginContent(registered = registered)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PluginContent(registered: RegisteredPlugin) {
    val reader =
        remember(registered.resourceRoot) {
            FileSystemResourceReader(registered.resourceRoot)
        }
    CompositionLocalProvider(LocalResourceReader provides reader) {
        // The plugin's own container, isolated from the host's: koinInject/koinViewModel inside the
        // plugin resolve here and nowhere else, so a plugin binding cannot override a host one.
        KoinIsolatedContext(context = registered.koinApplication) {
            // Built at load time from the server metadata, so nothing at render time influences it.
            registered.plugin.content(registered.context)
        }
    }
}
