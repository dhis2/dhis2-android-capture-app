package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.Serializable

/**
 * Named slots in the host app where a plugin's Composable UI can be rendered.
 *
 * A plugin declares the slots it targets in [PluginMetadata.injectionPoints].
 * The host app renders registered plugins at each slot via `PluginSlot`.
 */
@Serializable
enum class InjectionPoint {
    /** Rendered on the home screen, immediately above the program list. */
    HOME_ABOVE_PROGRAM_LIST,
}
