package org.dhis2.mobile.plugin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import org.dhis2.mobile.plugin.registry.forSlot
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
 * **The host decides how much room a plugin gets, not the plugin.** Each one is measured inside a
 * region bounded by [maxHeightFor], so a plugin that renders more than it was given cannot push the
 * host's own content off screen. A plugin author should not have to know this screen's layout to be
 * a good citizen on it — and asking every plugin to cap itself is a rule nothing could enforce,
 * since the plugins that matter are the ones the host never sees. Within that region the plugin is
 * free: filling it and scrolling inside it is correct, and because the bound is finite a plugin
 * calling `verticalScroll` does not meet an infinite height constraint.
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
    val slotPlugins = plugins.forSlot(injectionPoint)

    slotPlugins.forEach { registered ->
        // Keyed on the class loader as well as the id, because a reload replaces the registry entry
        // with a new InMemoryDexClassLoader and therefore new plugin classes. Keying on the id alone
        // kept the previous composition's `remember`ed slots alive across that swap, so state a
        // plugin had stored — a `produceState` value, for instance — was an instance of the *old*
        // loader's class while the new code cast it to the new loader's, giving
        // `ClassCastException: Foo cannot be cast to Foo`.
        key(registered.metadata.id, registered.classLoader) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeightFor(injectionPoint)),
            ) {
                PluginContent(registered = registered)
            }
        }
    }
}

/**
 * How much vertical room the host gives one plugin at [injectionPoint].
 *
 * A host layout fact, so it lives here rather than on [InjectionPoint] in the SDK — a plugin does
 * not need to know it, and the number is free to change with this screen without republishing the
 * plugin API.
 *
 * `HOME_ABOVE_PROGRAM_LIST` sits above the programme list on a screen the user opens to reach that
 * list, so the budget is generous enough for a card with a few rows and no more.
 * `DATA_SET_INSTANCE_CONTENT` never reaches here — it is a replacement slot, rendered by
 * [PluginReplacementSlot] inside the region its own host screen gives it.
 */
private fun maxHeightFor(injectionPoint: InjectionPoint): Dp =
    when (injectionPoint) {
        InjectionPoint.HOME_ABOVE_PROGRAM_LIST -> 240.dp
        InjectionPoint.DATA_SET_INSTANCE_CONTENT -> Dp.Unspecified
    }

/** Renders one plugin inside its own resource reader and Koin container. */
@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun PluginContent(registered: RegisteredPlugin) {
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
