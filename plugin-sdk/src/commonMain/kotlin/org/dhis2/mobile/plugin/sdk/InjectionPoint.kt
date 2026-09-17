package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.Serializable

/**
 * Named slots in the host app where a plugin's Composable UI can be rendered.
 *
 * A plugin declares the slots it targets in [PluginMetadata.injectionPoints]. Slots come in two
 * kinds: *additive* ones, where the host renders every registered plugin via `PluginSlot`, and
 * *replacement* ones, where a plugin takes over a region of a host screen and exactly one may win.
 *
 * A replacement slot is meaningless until an administrator says which objects it applies to, so it
 * sets [requiresConfiguration] and reads its targets from [PluginMetadata.slotConfig]. Its
 * configuration schema is its own — a list of data set UIDs here, something else for the next slot
 * — which is why the host stores that configuration unparsed and lets each slot decode it.
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
     * Replaces the body of the data set instance screen — the panes, section tabs and table.
     *
     * The host keeps everything framing it: the top bar with its title, back and sync actions, the
     * save button, the bottom bar with validation, completion and the non-editable reason, and the
     * snackbar. A data set no plugin claims keeps the default table.
     *
     * Configured by [DataSetInstanceSlotConfig]; the plugin reads which instance is open from
     * [DataSetInstanceSlotArguments].
     */
    DATA_SET_INSTANCE_CONTENT(requiresConfiguration = true),
}
