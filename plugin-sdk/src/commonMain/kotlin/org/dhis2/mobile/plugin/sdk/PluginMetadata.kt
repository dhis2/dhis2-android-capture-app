package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.Serializable

/**
 * Describes a plugin's identity, version, and distribution metadata.
 *
 * **Server-owned.** The DHIS2 administrator authors this as JSON in the server dataStore
 * (namespace `dhis2AndroidPlugins`, key `config`); it is the single source of truth. Plugins do not
 * declare any of it — the host reads it to decide what to download, verify and load, then hands it
 * to the plugin through [Dhis2PluginContext.pluginMetadata]. So there is exactly one place to change
 * a plugin's identity, and a plugin cannot rename itself into someone else's configuration.
 *
 * There is deliberately **no data-scope field here.** An earlier version carried
 * `allowedProgramUids` / `allowedDataSetUids`, which promised runtime enforcement that no longer
 * exists now that [Dhis2PluginContext.sdk] hands over the SDK unrestricted — and a grant nothing
 * checks is worse than no grant at all, because it reads like a control. Narrowing access is the
 * next iteration, and it will be enforced inside the SDK rather than by fields checked here.
 *
 * @property id Unique reverse-domain identifier, e.g. `org.myorg.my-plugin`.
 * @property version Semantic version string, e.g. `1.0.0`.
 * @property entryPoint Fully-qualified class name of the [Dhis2Plugin] implementation.
 * @property injectionPoints Slots in the host app where this plugin's UI will be rendered.
 * @property downloadUrl URL of the plugin bundle.
 * @property checksum SHA-256 checksum of the bundle, prefixed with `sha256:`.
 */
@Serializable
data class PluginMetadata(
    val id: String,
    val version: String,
    val entryPoint: String,
    val injectionPoints: List<InjectionPoint> = emptyList(),
    val downloadUrl: String = "",
    val checksum: String = "",
)
