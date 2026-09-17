package org.dhis2.mobile.plugin.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import org.dhis2.mobile.plugin.registry.selectReplacement
import org.dhis2.mobile.plugin.sdk.LocalHostRefresh
import org.dhis2.mobile.plugin.sdk.LocalSlotArguments
import org.dhis2.mobile.plugin.sdk.LocalSlotContentPadding
import org.dhis2.mobile.plugin.sdk.SlotArguments
import org.koin.compose.koinInject

/**
 * The plugin that replaces the host's own UI for [arguments], or null to keep it.
 *
 * Separate from [PluginReplacementSlot] because a host usually has to know the answer *before* it
 * composes the screen: whether a plugin owns the data set body also decides whether the host's view
 * model bothers building the tables underneath it. One lookup, both decisions, no way for them to
 * disagree.
 *
 * Reads the registry reactively, so a plugin loading later still takes over — but a host that
 * branches on this outside composition gets the value as of first composition, which is the normal
 * case: plugins are loaded at login, long before a data set can be opened.
 */
@Composable
fun rememberReplacementPlugin(
    arguments: SlotArguments,
    pluginRegistry: PluginRegistry = koinInject(),
): RegisteredPlugin? {
    val plugins by pluginRegistry.plugins.collectAsState()
    return remember(plugins, arguments) { plugins.selectReplacement(arguments) }
}

/**
 * Renders [plugin] in place of a region of a host screen.
 *
 * Unlike `PluginSlot`, which stacks every plugin registered for an additive slot, exactly one
 * plugin renders here — [rememberReplacementPlugin] has already chosen it. The host keeps whatever
 * frames the region: for the data set instance screen that is the top bar, the save button, the
 * bottom bar and the snackbar.
 *
 * [contentPadding] is the space that chrome occupies over the plugin, and [onHostRefresh] lets the
 * plugin ask the host to re-read the state it renders around it — the host does not observe writes
 * the plugin makes through the SDK.
 */
@Composable
fun PluginReplacementSlot(
    plugin: RegisteredPlugin,
    arguments: SlotArguments,
    contentPadding: PaddingValues = PaddingValues(),
    onHostRefresh: () -> Unit = {},
) {
    // Keyed exactly as PluginSlot is, and for the same reason: a reload replaces the plugin's
    // classes wholesale, and state remembered across that swap belongs to the old loader.
    key(plugin.metadata.id, plugin.classLoader) {
        CompositionLocalProvider(
            LocalSlotArguments provides arguments,
            LocalSlotContentPadding provides contentPadding,
            LocalHostRefresh provides onHostRefresh,
        ) {
            PluginContent(registered = plugin)
        }
    }
}
