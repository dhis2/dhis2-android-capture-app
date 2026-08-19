package org.dhis2.mobile.plugin.sdk

import kotlinx.serialization.Serializable

/**
 * The slice of DHIS2 data a plugin is allowed to reach.
 *
 * **Server-owned**, like the rest of [PluginMetadata]: the administrator authors it as JSON in the
 * dataStore, and the host turns it into the scope the DHIS2 SDK enforces. A plugin never declares
 * its own scope and cannot widen the one it is given.
 *
 * Every dimension defaults to the closed value, so an omitted `scope` block grants nothing. The
 * shape is deliberately flat rather than polymorphic, because it is written by hand:
 *
 * ```json
 * "scope": {
 *   "programs":     { "uids": ["IpHINAT79UW"] },
 *   "orgUnits":     { "uids": ["O6uvpzGd5pu"], "mode": "DESCENDANTS" },
 *   "capabilities": ["READ_METADATA", "READ_TRACKED_ENTITY", "READ_EVENT"],
 *   "writable":     { "dataSets": { "uids": ["BfMAe6Itzgt"] } }
 * }
 * ```
 *
 * @property programs Programs whose tracked entities, enrollments and events are readable.
 * @property dataSets Data sets whose data values are readable.
 * @property trackedEntityTypes Tracked entity types that are readable. Defaults to all, since
 *   [programs] already bounds the tracker data.
 * @property dataElements Data elements that are readable, within the bound [dataSets] already sets.
 * @property orgUnits Organisation units whose data is readable.
 * @property writable The subset of the above that may also be written. Always intersected with the
 *   read grant, so listing something here that is not readable has no effect.
 * @property capabilities Feature areas this scope unlocks, by [PluginCapability] name. Empty means
 *   nothing is exposed; names the host does not recognise are ignored with a warning.
 */
@Serializable
data class PluginScope(
    val programs: UidGrant = UidGrant.NONE,
    val dataSets: UidGrant = UidGrant.NONE,
    val trackedEntityTypes: UidGrant = UidGrant.ALL,
    val dataElements: UidGrant = UidGrant.ALL,
    val orgUnits: OrgUnitGrant = OrgUnitGrant.NONE,
    val writable: WritableGrant = WritableGrant(),
    val capabilities: List<String> = emptyList(),
)

/**
 * A set of metadata UIDs, or "all of them".
 *
 * `{}` grants nothing, `{"uids": ["a", "b"]}` grants those two, `{"all": true}` applies no
 * restriction. [all] wins if both are given.
 */
@Serializable
data class UidGrant(
    val all: Boolean = false,
    val uids: List<String> = emptyList(),
) {
    companion object {
        val NONE = UidGrant()
        val ALL = UidGrant(all = true)
    }
}

/**
 * An organisation unit grant.
 *
 * Unlike [UidGrant] this carries a hierarchy [mode], because granting an org unit usually means
 * granting the sub-tree below it. The host expands the hierarchy; the config only names the roots.
 *
 * @property mode One of `SELECTED`, `CHILDREN` or `DESCENDANTS`. Defaults to `DESCENDANTS`.
 *   Unrecognised values fall back to `DESCENDANTS` with a warning.
 */
@Serializable
data class OrgUnitGrant(
    val all: Boolean = false,
    val capture: Boolean = false,
    val uids: List<String> = emptyList(),
    val mode: String = "DESCENDANTS",
) {
    companion object {
        val NONE = OrgUnitGrant()
    }
}

/** The writable subset of a [PluginScope]. Intersected with the read grant, so it can only narrow. */
@Serializable
data class WritableGrant(
    val programs: UidGrant = UidGrant.NONE,
    val dataSets: UidGrant = UidGrant.NONE,
    val orgUnits: OrgUnitGrant = OrgUnitGrant.NONE,
)

/**
 * The feature areas a [PluginScope] can unlock.
 *
 * Held as strings in [PluginScope.capabilities] rather than as this enum, so that a config written
 * against a newer host does not fail to parse on an older one — an unknown name is ignored with a
 * warning rather than taking the whole plugin down.
 */
enum class PluginCapability {
    READ_METADATA,
    READ_TRACKED_ENTITY,
    READ_ENROLLMENT,
    READ_EVENT,
    READ_DATA_VALUE,
    SEARCH_TRACKED_ENTITY,
    READ_RELATIONSHIP,
    READ_FILE_RESOURCE,
    WRITE_TRACKED_ENTITY,
    WRITE_ENROLLMENT,
    WRITE_EVENT,
    WRITE_DATA_VALUE,
}
