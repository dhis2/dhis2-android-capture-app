package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.Serializable

/**
 * Named slots in the host app where a plugin's Composable UI can be rendered.
 *
 * A plugin declares the slots it targets in [PluginMetadata.injectionPoints]. Slots come in two
 * kinds: *additive* ones, where the host stacks every registered plugin, and *replacement* ones,
 * where one plugin takes over a region of a host screen.
 *
 * A replacement slot is meaningless until an administrator says which objects it applies to, so it
 * sets [requiresConfiguration] and reads its targets from [PluginMetadata.slotConfig]. Each slot
 * owns its configuration schema, which is why the host stores it unparsed.
 *
 * @property requiresConfiguration Whether the slot renders nothing until it is configured. `false`
 *   for additive slots, which apply everywhere they occur.
 */
@Serializable
enum class InjectionPoint(
    val requiresConfiguration: Boolean,
) {
    /** Rendered on the home screen, immediately above the program list. Additive. */
    HOME_ABOVE_PROGRAM_LIST(requiresConfiguration = false),

    /**
     * Replaces the body of the data set instance screen — the panes, section tabs and table. The
     * host keeps the top bar, save button, bottom bar and snackbar; an unclaimed data set keeps the
     * default table.
     *
     * Configured by [DataSetInstanceSlotConfig], rendered with [DataSetInstanceSlotArguments].
     */
    DATA_SET_INSTANCE_CONTENT(requiresConfiguration = true),
}
