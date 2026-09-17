package org.dhis2.mobile.plugin.sdk

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * What the host knows about the occurrence being rendered — which data set instance is open, which
 * program, and so on, depending on the slot.
 *
 * A plugin reads it inside `content`, casting to the type its slot provides:
 *
 * ```kotlin
 * @Composable
 * override fun content(context: Dhis2PluginContext) {
 *     val args = LocalSlotArguments.current as? DataSetInstanceSlotArguments ?: return
 *     val values = context.sdk.dataValueModule().dataValues()
 *         .byDataSetUid().eq(args.dataSetUid)
 *         .blockingGet()
 * }
 * ```
 *
 * `as?` rather than a checked accessor, and null rather than a cast failure: a plugin renders inside
 * the host's composition, where an exception takes down a screen still showing the host's own top
 * bar, save button and validation state.
 *
 * It is also the half of a slot's contract that decides *where* a plugin renders: [appliesTo]
 * answers that against the administrator's configuration for this slot. Keeping the rule here, on
 * the arguments, is what lets the host render any slot without knowing what a data set is — adding
 * a slot means adding an arguments type and a configuration type, and touching nothing else.
 */
interface SlotArguments {
    /** The slot these arguments describe. */
    val injectionPoint: InjectionPoint

    /**
     * Whether a plugin whose configuration for [injectionPoint] is [config] renders here.
     *
     * [config] is the raw entry from [PluginMetadata.slotConfig], or `null` when the administrator
     * configured nothing. Implementations decode it themselves and must not throw: a malformed
     * configuration means "does not apply", never a crash inside the host's composition.
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
 * Lenient on purpose: a slot configuration field an older plugin does not understand is not an
 * error.
 *
 * Note there is no `inline` helper anywhere in this artifact, however convenient a
 * `currentAs<T>()` would read. This module is compiled at the host's JVM target, plugins are
 * compiled at their own and usually lower, and inlining across that boundary fails to compile in
 * the plugin's build with a message about `-jvm-target` that says nothing about slots.
 */
internal val slotConfigJson: Json = Json { ignoreUnknownKeys = true }
