package org.dhis2.mobile.plugin.sdk

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * What the host knows about the occurrence being rendered — which data set instance is open, and so
 * on depending on the slot. A plugin reads it from [LocalSlotArguments] and casts with `as?`:
 *
 * ```kotlin
 * val args = LocalSlotArguments.current as? DataSetInstanceSlotArguments ?: return
 * ```
 *
 * [appliesTo] keeps the "does this plugin claim this occurrence" rule here, so the host can render
 * any slot without knowing what a data set is.
 */
interface SlotArguments {
    /** The slot these arguments describe. */
    val injectionPoint: InjectionPoint

    /**
     * Whether a plugin whose configuration for [injectionPoint] is [config] renders here.
     *
     * [config] is the raw entry from [PluginMetadata.slotConfig], `null` when nothing was
     * configured. Implementations must not throw: a malformed configuration means "does not apply".
     */
    fun appliesTo(config: JsonObject?): Boolean
}

/**
 * The arguments for the current slot, or `null` outside one.
 *
 * `static` because the value never changes for the life of a slot's composition.
 */
val LocalSlotArguments: ProvidableCompositionLocal<SlotArguments?> =
    staticCompositionLocalOf { null }

/**
 * Space the host's own chrome occupies over the plugin's content — today, the floating save button.
 *
 * A plugin's scrolling content should add it to its bottom padding so the last row is reachable.
 */
val LocalSlotContentPadding: ProvidableCompositionLocal<PaddingValues> =
    staticCompositionLocalOf { PaddingValues() }

/**
 * Asks the host to re-read the state it renders around the plugin.
 *
 * The host does not observe what a plugin writes through the SDK, so a plugin calls this after a
 * write that changes what the chrome shows — completing a data set, reopening it, or anything
 * affecting whether it is editable. No-op outside a slot.
 */
val LocalHostRefresh: ProvidableCompositionLocal<() -> Unit> =
    staticCompositionLocalOf { {} }

/**
 * Lenient on purpose: a configuration field an older plugin does not understand is not an error.
 *
 * Nothing here is `inline` — plugins compile at a lower JVM target than the host, and inlining
 * across that boundary fails their build with a `-jvm-target` error that mentions nothing of slots.
 */
internal val slotConfigJson: Json = Json { ignoreUnknownKeys = true }
