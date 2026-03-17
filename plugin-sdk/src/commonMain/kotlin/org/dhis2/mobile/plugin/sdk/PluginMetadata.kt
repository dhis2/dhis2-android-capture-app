package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.Serializable

/**
 * Describes the plugin's identity, version, data scope, and distribution metadata.
 *
 * This object is both embedded in the plugin JAR (via [Dhis2Plugin.metadata]) and
 * declared in the App Hub manifest. The host app validates consistency between the two.
 *
 * @property id Unique reverse-domain identifier, e.g. `org.myorg.my-plugin`.
 * @property version Semantic version string, e.g. `1.0.0`.
 * @property entryPoint Fully-qualified class name of the [Dhis2Plugin] implementation.
 * @property allowedProgramUids Programs the plugin is allowed to read/write. Enforced at runtime.
 * @property allowedDataSetUids Data sets the plugin is allowed to read/write. Enforced at runtime.
 * @property injectionPoints Slots in the host app where this plugin's UI will be rendered.
 * @property downloadUrl URL of the plugin DEX file on the App Hub.
 * @property checksum SHA-256 checksum of the plugin DEX file, prefixed with `sha256:`.
 */
@Serializable
data class PluginMetadata(
    val id: String,
    val version: String,
    val entryPoint: String,
    val allowedProgramUids: List<String> = emptyList(),
    val allowedDataSetUids: List<String> = emptyList(),
    val injectionPoints: List<InjectionPoint> = emptyList(),
    val downloadUrl: String = "",
    val checksum: String = "",
)
