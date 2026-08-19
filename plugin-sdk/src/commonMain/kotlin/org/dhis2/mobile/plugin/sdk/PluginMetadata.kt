package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.Serializable

/**
 * Describes a plugin's identity, version, data scope, and distribution metadata.
 *
 * **Server-owned.** The DHIS2 administrator authors this as JSON in the server dataStore
 * (namespace `dhis2AndroidPlugins`, key `config`); it is the single source of truth. Plugins do
 * not declare any of it — the host reads it to decide what to download, verify and load, then
 * hands it to the plugin. In particular [scope] is *granted* by the server, so a plugin cannot
 * widen its own access.
 *
 * @property id Unique reverse-domain identifier, e.g. `org.myorg.my-plugin`.
 * @property version Semantic version string, e.g. `1.0.0`.
 * @property entryPoint Fully-qualified class name of the `Dhis2Plugin` implementation.
 * @property scope The data the plugin may reach. Absent means fall back to [allowedProgramUids] and
 *   [allowedDataSetUids]; see [effectiveScope].
 * @property allowedProgramUids Programs the plugin may read. Superseded by [scope].
 * @property allowedDataSetUids Data sets the plugin may read and write. Superseded by [scope].
 * @property injectionPoints Slots in the host app where this plugin's UI will be rendered.
 * @property downloadUrl URL of the plugin bundle.
 * @property checksum SHA-256 checksum of the plugin bundle, prefixed with `sha256:`.
 */
@Serializable
data class PluginMetadata(
    val id: String,
    val version: String,
    val entryPoint: String,
    val scope: PluginScope? = null,
    @Deprecated("Use scope.programs instead. Kept so existing dataStore configs keep working.")
    val allowedProgramUids: List<String> = emptyList(),
    @Deprecated("Use scope.dataSets instead. Kept so existing dataStore configs keep working.")
    val allowedDataSetUids: List<String> = emptyList(),
    val injectionPoints: List<InjectionPoint> = emptyList(),
    val downloadUrl: String = "",
    val checksum: String = "",
) {
    /**
     * The scope to enforce, whichever form the config was written in.
     *
     * A config with no `scope` block predates it, so it is read as the grant that reproduces the
     * behaviour it used to get: read access across the listed programs, read and write on the
     * listed data sets, and no organisation unit restriction — because there was none. New configs
     * should set [scope] explicitly and get closed-by-default everywhere.
     */
    @Suppress("DEPRECATION")
    val effectiveScope: PluginScope
        get() =
            scope ?: PluginScope(
                programs = UidGrant(uids = allowedProgramUids),
                dataSets = UidGrant(uids = allowedDataSetUids),
                orgUnits = OrgUnitGrant(all = true),
                writable = WritableGrant(dataSets = UidGrant(uids = allowedDataSetUids)),
                capabilities = LEGACY_CAPABILITIES,
            )

    private companion object {
        val LEGACY_CAPABILITIES =
            listOf(
                PluginCapability.READ_METADATA,
                PluginCapability.READ_TRACKED_ENTITY,
                PluginCapability.READ_ENROLLMENT,
                PluginCapability.READ_EVENT,
                PluginCapability.READ_DATA_VALUE,
                PluginCapability.WRITE_DATA_VALUE,
            ).map { it.name }
    }
}
