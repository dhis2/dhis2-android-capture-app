package org.dhis2.mobile.plugin.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import org.dhis2.mobile.plugin.registry.RegisteredPlugin
import org.dhis2.mobile.plugin.sdk.LocalHostRefresh
import org.dhis2.mobile.plugin.sdk.LocalSlotArguments
import org.dhis2.mobile.plugin.sdk.LocalSlotContentPadding
import org.dhis2.mobile.plugin.sdk.SlotArguments

/**
 * Renders [plugin] in place of a region of a host screen.
 *
 * Unlike [PluginSlot], which stacks every plugin registered for an additive slot, exactly one
 * plugin renders here — the host has already chosen it with
 * [org.dhis2.mobile.plugin.registry.selectReplacement].
 *
 * [contentPadding] is the space the host's chrome occupies over the plugin, and [onHostRefresh]
 * lets the plugin ask the host to re-read the state it renders around it.
 */
@Composable
fun PluginReplacementSlot(
    plugin: RegisteredPlugin,
    arguments: SlotArguments,
    contentPadding: PaddingValues = PaddingValues(),
    onHostRefresh: () -> Unit = {},
) {
    // Keyed as PluginSlot is: a reload replaces the plugin's classes, and state remembered across
    // that swap belongs to the old loader.
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
